package com.backendify.proxy.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS);  // Cache duration of 24 hours

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("companyCache");
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
