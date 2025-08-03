package com.company.calendar.service.appointment;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LockManager {
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public Object getLock(String ownerId) {
        return locks.computeIfAbsent(ownerId, k -> new Object());
    }
}
