package com.intenthealer.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for heal caching.
 */
public class CacheConfig {

    @JsonProperty("enabled")
    private boolean enabled = true;

    @JsonProperty("ttl_hours")
    private int ttlHours = 24;

    @JsonProperty("max_entries")
    private int maxEntries = 10000;

    @JsonProperty("storage")
    private StorageType storage = StorageType.MEMORY;

    @JsonProperty("file_path")
    private String filePath = ".healer/cache";

    @JsonProperty("redis_url")
    private String redisUrl;

    public CacheConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTtlHours() {
        return ttlHours;
    }

    public void setTtlHours(int ttlHours) {
        this.ttlHours = ttlHours;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public StorageType getStorage() {
        return storage;
    }

    public void setStorage(StorageType storage) {
        this.storage = storage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public void setRedisUrl(String redisUrl) {
        this.redisUrl = redisUrl;
    }

    @Override
    public String toString() {
        return "CacheConfig{enabled=" + enabled + ", ttlHours=" + ttlHours +
               ", storage=" + storage + "}";
    }

    /**
     * Cache storage types.
     */
    public enum StorageType {
        MEMORY,
        FILE,
        REDIS
    }
}
