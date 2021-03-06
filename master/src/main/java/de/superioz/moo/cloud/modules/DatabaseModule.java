package de.superioz.moo.cloud.modules;

import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.ExtendedLogger;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.api.module.ModuleDependency;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.*;
import de.superioz.moo.cloud.events.DatabaseConnectionEvent;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@ModuleDependency(modules = {"config", "redis"})
@Getter
public class DatabaseModule extends Module {

    private Map<String, DatabaseCollection> collectionMap = new HashMap<>();
    private DatabaseConnection dbConn;
    private JsonConfig config;

    private ExtendedLogger logger;

    public DatabaseModule(JsonConfig config, ExtendedLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "database";
    }

    @Override
    protected void onEnable() {
        this.dbConn = new DatabaseConnection.Builder()
                .logger(false)
                .host(config.get("database.hostname")).port(config.get("database.port"))
                .database(config.get("database.database"))
                .user(config.get("database.user"))
                .password(config.get("database.password"))
                .build();

        // connects to the database
        // and list collection for connection check
        getLogger().info("Connecting to database .. [" + dbConn.getHost() + ":" + dbConn.getPort() + "]" +
                "{user: '" + dbConn.getUser() + "', pw: '" + dbConn.getPassword() + "', db: '" + dbConn.getDatabase() + "'}");
        dbConn.connect(dbWorker -> {
            int size = dbWorker.getCollections().size();
            Cloud.getInstance().getLogger().info("Connected to database! [" + dbWorker.getUser() + ":" + dbWorker.getDatabase() + "@" + dbWorker.getHost() + "]");
            Cloud.getInstance().getLogger().debug("Collections (" + size + "): " + dbWorker.getCollections());

            // register database modules
            getLogger().debug("Registering database collections ..");
            this.registerDatabaseCollections(
                    new GroupCollection(dbConn),
                    new PlayerDataCollection(dbConn),
                    new BanCollection(dbConn),
                    new BanArchiveCollection(dbConn),
                    new PatternCollection(dbConn)
            );
            getLogger().debug("Finished registering database collections. (" + collectionMap.size() + ")");

            // init collections class
            DatabaseCollections.init(this);

            // database connection event
            EventExecutor.getInstance().execute(new DatabaseConnectionEvent(dbConn, true));
        });
    }

    @Override
    protected void onDisable() {
        collectionMap.clear();
        if(dbConn != null) dbConn.disconnect();
    }

    /**
     * Registers given database collections
     *
     * @param collections The collections
     */
    public void registerDatabaseCollections(DatabaseCollection... collections) {
        for(DatabaseCollection collection : collections) {
            collectionMap.put(collection.getName(), collection);
        }
    }

    /**
     * Gets the collection with given wrapped class
     *
     * @param type The database type
     * @return The collection
     */
    public <K, V> DatabaseCollection<K, V> getCollection(DatabaseType type) {
        return getCollectionMap().get(type.getName());
    }

}
