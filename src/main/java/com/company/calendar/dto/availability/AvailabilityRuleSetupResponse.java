package com.company.calendar.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class AvailabilityRuleSetupResponse {
    @Schema(description = "Indicates if operation was successful")
    private final boolean success;
    @Schema(description = "Response message")
    private final String message;
}
