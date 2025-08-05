package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilityRuleSetupResponse {
    private boolean success;
    private String message;
}
