package com.company.calendar.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> appointmentLockMap() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, String> appointmentIdempotencyStore() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, Object> appointmentOwnerLockMap() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }
}

