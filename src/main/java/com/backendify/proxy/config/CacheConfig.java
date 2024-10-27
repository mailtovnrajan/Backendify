package com.backendify.proxy.config;

import com.backendify.proxy.model.CompanyResponse;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("companyCache");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfter(new Expiry<Object, Object>() {
                    @Override
                    public long expireAfterCreate(@NonNull Object key, @NonNull Object value, long currentTime) {
                        CompanyResponse valueAsCompanyResponse = (CompanyResponse) value;

                        if (valueAsCompanyResponse.isActive()) {
                            LocalDateTime activeUntil = LocalDateTime.parse(valueAsCompanyResponse.getActiveUntil(), DateTimeFormatter.ISO_DATE_TIME);
                            return activeUntil.getNano() - LocalDateTime.now().getNano();
                        } else {
                            return TimeUnit.HOURS.toNanos(24);
                        }
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull Object key, @NonNull Object value, long currentTime, @NonNegative long currentDuration) {
                        CompanyResponse valueAsCompanyResponse = (CompanyResponse) value;

                        if (valueAsCompanyResponse.isActive()) {
                            LocalDateTime activeUntil = LocalDateTime.parse(valueAsCompanyResponse.getActiveUntil(), DateTimeFormatter.ISO_DATE_TIME);
                            return activeUntil.getNano() - LocalDateTime.now().getNano();
                        } else {
                            return TimeUnit.HOURS.toNanos(24);
                        }
                    }

                    @Override
                    public long expireAfterRead(@NonNull Object key, @NonNull Object value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                });
    }
}