package com.company.calendar.service.appointment;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AppointmentIdempotencyStore {

    private final Cache<String, String> appointmentIdempotencyCache;

    public AppointmentIdempotencyStore() {
        this.appointmentIdempotencyCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public String get(String key) {
        return appointmentIdempotencyCache.getIfPresent(key);
    }

    public void put(String key, String value) {
        appointmentIdempotencyCache.put(key, value);
    }
}