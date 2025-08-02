package com.company.calendar.entity;

import com.company.calendar.enums.RuleType;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
@Getter
public class AvailabilityRule {
    private String ownerId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private RuleType ruleType; // AVAILABLE or UNAVAILABLE
}