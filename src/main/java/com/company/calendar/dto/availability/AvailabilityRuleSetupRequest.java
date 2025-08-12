package com.company.calendar.dto.availability;

import com.company.calendar.validator.ValidAvailabilityRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;
import java.time.LocalTime;

import static com.company.calendar.constants.ApplicationConstants.ALPHANUMERIC_HYPHEN_UNDERSCORE_REGEX;

@Getter
@ValidAvailabilityRules
@Builder
public final class AvailabilityRuleSetupRequest {

    @NotBlank(message = "Owner Id must not be blank")
    @Pattern(regexp = ALPHANUMERIC_HYPHEN_UNDERSCORE_REGEX, message = "Owner Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
    @Schema(example = "1", description = "Owner ID who owns the availability rules")
    private final String ownerId;

    @NotNull(message = "Rules list must not be null")
    @Size(max = 30, message = "A maximum of 30 rules per user is allowed")
    @Valid
    @Schema(description = "List of availability rules", requiredMode = Schema.RequiredMode.REQUIRED)
    private final List<AvailabilityRuleRequest> rules;

    @Getter
    @Builder
    public static class AvailabilityRuleRequest {
        @Schema(example = "MONDAY", description = "Day of the week")
        private final DayOfWeek dayOfWeek;
        @Schema(example = "16:00", description = "Start time in HH:mm format")
        private final LocalTime startTime;
        @Schema(example = "23:00", description = "End time in HH:mm format")
        private final LocalTime endTime;
    }
}
