package com.company.calendar.repository.availabilityRule;

import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAvailabilityRuleRepository implements AvailabilityRuleRepository {

    private final Map<String, List<AvailabilityRule>> store = new ConcurrentHashMap<>();

    // Create only if ownerId doesn't already exist
    @Override
    public void create(String ownerId, List<AvailabilityRule> rules) {
        if (store.containsKey(ownerId)) {
            throw new IllegalStateException("Availability rules already exist for ownerId: " + ownerId);
        }
        store.put(ownerId, rules);
    }

    // Overwrite or insert
    @Override
    public void upsert(String ownerId, List<AvailabilityRule> rules) {
        store.put(ownerId, rules); // Always replaces the existing rules
    }

    public List<AvailabilityRule> findByOwnerId(String ownerId) {
        return store.getOrDefault(ownerId, Collections.emptyList());
    }

    @Override
    public List<AvailabilityRule> findByOwnerIdAndDayOfWeekAndRuleType(String ownerId, DayOfWeek dayOfWeek, RuleType ruleType) {
        List<AvailabilityRule> rules = store.getOrDefault(ownerId, Collections.emptyList());
        return rules.stream().filter(rule ->
                rule.getDayOfWeek().equals(dayOfWeek)
                        && rule.getRuleType() == ruleType)
                .toList();
    }
}
