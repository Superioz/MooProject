package de.superioz.moo.cloud.listeners;

import de.superioz.moo.network.common.MooCache;
import de.superioz.moo.network.config.NetworkConfig;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.cloud.events.DatabaseConnectionEvent;

/**
 * Listens to the database being connected/disconnected
 */
public class DatabaseConnectionListener implements EventListener {

    @EventHandler
    public void onDatabaseConnection(DatabaseConnectionEvent event) {
        // ..

        if(event.isConnectionActive()) {
            // if the cloud connected to database we want to set config information into the cache
            Cloud.getInstance().setNetworkConfig(new NetworkConfig(event.getConnection()));
            Cloud.getInstance().getNetworkConfig().load();

            // server patterns into redis cache
            DatabaseCollections.PATTERN.list().forEach(pattern ->
                    MooCache.getInstance().getPatternMap().putAsync(pattern.getName(), pattern));
        }
    }

}
