package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpdateAvailabilityRulesResult {
    private String message;
    private boolean created;
}