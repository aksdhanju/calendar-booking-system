package com.company.calendar.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookAppointmentRequest {
    @NotBlank
    private String ownerId;

    @NotBlank
    private String inviteeId;

    @NotNull
    private LocalDateTime startTime;
}
