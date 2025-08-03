package com.company.calendar.dto.availability;

import com.company.calendar.validator.ValidAvailabilityRules;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Owner ID must not be blank")
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
