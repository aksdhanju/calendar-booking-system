package com.company.calendar.repository.availabilityRule;

import com.company.calendar.entity.AvailabilityRule;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRuleRepository {

    void save(String ownerId, List<AvailabilityRule> rules);

    List<AvailabilityRule> findByOwnerId(String ownerId);

    List<AvailabilityRule> findByOwnerIdAndDayOfWeek(String ownerId, DayOfWeek dayOfWeek);
}
