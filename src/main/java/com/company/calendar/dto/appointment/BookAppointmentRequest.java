package com.company.calendar.dto.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.company.calendar.constants.ApplicationConstants.ALPHANUMERIC_HYPHEN_UNDERSCORE_REGEX;
import static com.company.calendar.constants.ApplicationConstants.YYYY_MM_DD_HH_MM_SS_FORMAT;

@Getter
@Builder
public class BookAppointmentRequest {
    @NotBlank
    @Pattern(regexp = ALPHANUMERIC_HYPHEN_UNDERSCORE_REGEX, message = "Owner Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
    private String ownerId;

    @NotBlank
    @Pattern(regexp = ALPHANUMERIC_HYPHEN_UNDERSCORE_REGEX, message = "Owner Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
    private String inviteeId;

    @NotNull
    @Future
    @JsonFormat(pattern = YYYY_MM_DD_HH_MM_SS_FORMAT)
    private LocalDateTime startDateTime;
}
