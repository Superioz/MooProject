package de.superioz.moo.cloud.database;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.*;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.cloud.database.cache.PlayerDataCache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerDataCollection extends DatabaseCollection<UUID, PlayerData> {

    public PlayerDataCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(PlayerData.class);
        super.cache(new PlayerDataCache(new DatabaseCache.Builder().database(this).expireAfterAccess(1, TimeUnit.HOURS)));
    }

    @Override
    public String getName() {
        return DatabaseType.PLAYER.getName();
    }

    /**
     * Get the currentData
     *
     * @param id The data to identify
     * @return The playerData
     */
    public PlayerData getCurrentData(PlayerData id, boolean update) {
        if(id == null) return null;
        UUID uuid = id.uuid;
        String name = id.lastName;

        // fail
        if(uuid == null) return null;

        PlayerData currentData = get(uuid);
        if(currentData == null) {
            Group def = CloudCollections.groups().getDefault();
            id.group = def.name;
            id.rank = def.rank;
            id.lastOnline = 0L;
            id.joined = System.currentTimeMillis();
            id.firstOnline = System.currentTimeMillis();
            id.totalOnline = 0L;
            id.coins = 0L;

            set(uuid, id, true);
            currentData = id;
        }
        else if(update) {
            DbQuery updates = new DbQuery(currentData.getClass());

            if(!currentData.lastName.equals(id.lastName)) {
                updates.equate(DbModifier.PLAYER_NAME, name);
            }
            if(!currentData.lastip.equals(id.lastip)) {
                updates.equate(DbModifier.PLAYER_IP, id.lastip);
            }
            if(!CloudCollections.groups().has(currentData.group)) {
                Group def = CloudCollections.groups().getDefault();
                updates.equate(DbModifier.PLAYER_GROUP, def.name).equate(DbModifier.PLAYER_RANK, def.rank);
                currentData.group = def.name;
            }

            set(uuid, currentData, updates.toMongoQuery().build(), true);
        }
        return currentData;
    }

}
