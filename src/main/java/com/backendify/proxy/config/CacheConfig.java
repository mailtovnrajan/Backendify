package com.backendify.proxy.config;

import com.backendify.proxy.model.CompanyResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheConfig {

    private final ConcurrentHashMap<String, CacheEntry> cacheMap = new ConcurrentHashMap<>();

    // TTL for cache entries (24 hours)
    private static final long CACHE_TTL_IN_SECONDS = 24 * 60 * 60;

    // Method to retrieve from cache
    public CompanyResponse get(String key) {
        CacheEntry entry = cacheMap.get(key);
        if (entry == null || isExpired(entry)) {
            cacheMap.remove(key);
            return null;
        }
        return entry.getValue();
    }

    // Method to add to cache
    public void put(String key, CompanyResponse value) {
        cacheMap.put(key, new CacheEntry(value, LocalDateTime.now()));
    }

    // Check if cache entry is expired
    private boolean isExpired(CacheEntry entry) {
        return LocalDateTime.now().isAfter(entry.getTimestamp().plusSeconds(CACHE_TTL_IN_SECONDS));
    }

    // Inner class representing a cache entry
    private static class CacheEntry {
        private final CompanyResponse value;
        private final LocalDateTime timestamp;

        public CacheEntry(CompanyResponse value, LocalDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public CompanyResponse getValue() {
            return value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
