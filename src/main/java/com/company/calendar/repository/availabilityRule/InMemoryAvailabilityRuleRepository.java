package com.company.calendar.repository.availabilityRule;

import com.company.calendar.entity.AvailabilityRule;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAvailabilityRuleRepository implements AvailabilityRuleRepository {

    private final Map<String, List<AvailabilityRule>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String ownerId, List<AvailabilityRule> rules) {
        store.put(ownerId, rules);
    }

    public boolean saveIfAbsent(String ownerId, List<AvailabilityRule> rules) {
        // Only put if absent
        return store.computeIfAbsent(ownerId, id -> rules) == rules;
    }

    public List<AvailabilityRule> findByOwnerId(String ownerId) {
        return store.getOrDefault(ownerId, List.of());
    }

    @Override
    public List<AvailabilityRule> findByOwnerIdAndDayOfWeek(String ownerId, DayOfWeek dayOfWeek) {
        List<AvailabilityRule> rules = store.getOrDefault(ownerId, List.of());
        return rules.stream().filter(rule ->
                rule.getDayOfWeek().equals(dayOfWeek))
                .toList();
    }
}
