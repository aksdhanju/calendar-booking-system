package com.company.calendar.service.appointment;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AppointmentIdempotencyLockManager {
    private final Cache<String, Object> appointmentLockCache;

    public AppointmentIdempotencyLockManager() {
        this.appointmentLockCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public Object getLock(String key) {
        return appointmentLockCache.get(key, k -> new Object());
    }

    public void releaseLock(String key) {
        appointmentLockCache.invalidate(key);
    }
}