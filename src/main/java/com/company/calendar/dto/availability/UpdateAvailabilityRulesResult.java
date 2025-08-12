package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class UpdateAvailabilityRulesResult {
    private final String message;
    private final boolean created;
}