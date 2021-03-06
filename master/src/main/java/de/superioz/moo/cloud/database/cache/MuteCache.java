package de.superioz.moo.cloud.database.cache;

import de.superioz.moo.api.database.DatabaseCache;
import de.superioz.moo.api.database.objects.Ban;

import java.util.UUID;

public class MuteCache extends DatabaseCache<UUID, Ban> {

    public MuteCache(DatabaseCache.Builder builder) {
        super(builder);
    }

}
