package com.company.calendar.repository.availabilityRule;

import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRuleRepository {

    void create(String ownerId, List<AvailabilityRule> rules);

    // Overwrite or insert
    void upsert(String ownerId, List<AvailabilityRule> rules);

    List<AvailabilityRule> findByOwnerId(String ownerId);

    List<AvailabilityRule> findByOwnerIdAndDayOfWeekAndRuleType(String ownerId, DayOfWeek dayOfWeek, RuleType ruleType);
}
