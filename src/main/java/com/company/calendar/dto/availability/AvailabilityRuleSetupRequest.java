package com.company.calendar.dto.availability;

import com.company.calendar.validator.ValidAvailabilityRules;
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

@Getter
@ValidAvailabilityRules
@Builder
public class AvailabilityRuleSetupRequest {

    @NotBlank(message = "Owner Id must not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Owner Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
    private String ownerId;

    @NotNull(message = "Rules list must not be null")
    @Size(max = 30, message = "A maximum of 30 rules per user is allowed")
    @Valid
    private List<AvailabilityRuleRequest> rules;

    @Getter
    @Builder
    public static class AvailabilityRuleRequest {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
