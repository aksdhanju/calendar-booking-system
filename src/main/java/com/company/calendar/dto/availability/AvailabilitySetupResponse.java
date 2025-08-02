package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilitySetupResponse {
    private boolean success;
    private String message;
}
