package de.superioz.moo.api.cache;

public enum RedisConfig {

    /**
     * @see MooCache#groupMap
     */
    GROUP_MAP,

    /**
     * @see MooCache#uniqueIdPlayerMap
     */
    PLAYER_DATA_MAP,

    /**
     * @see MooCache#nameUniqueIdMap
     */
    PLAYER_ID_MAP,

    /**
     * @see MooCache#playerPermissionMap
     */
    PLAYER_PERMISSION_MAP,

    /**
     * @see MooCache#configMap
     */
    CONFIG_MAP,

    /**
     * @see MooCache#serverMap
     */
    SERVER_MAP;

    public static final String REDIS_PATH = "redis.";
    public static final String REDIS_CONFIG_PATH = "config.";

    RedisConfig() {
    }

    /**
     * Gets the key of the map for cache storing
     *
     * @return The key as string
     */
    public String getKey() {
        return name().toLowerCase();
    }

    /**
     * Gets the path to fetch it from a config file
     *
     * @return The path as string
     */
    public String getPath() {
        return REDIS_PATH + REDIS_CONFIG_PATH + getKey();
    }

}