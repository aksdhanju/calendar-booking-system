package com.company.calendar.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilitySetupResponse {
    private boolean success;
    private String message;
}
