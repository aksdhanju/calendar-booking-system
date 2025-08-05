package com.company.calendar.dto.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookAppointmentRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Owner Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
    private String ownerId;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Owner Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
    private String inviteeId;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDateTime;
}
