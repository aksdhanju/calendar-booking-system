package com.company.calendar.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class UpcomingAppointmentResponse {

    @Schema(description = "Unique identifier of the appointment", example = "1")
    private final String appointmentId;

    @Schema(description = "Start time of the appointment in yyyy-MM-dd HH:mm:ss format", example = "2025-08-15 14:00:00")
    private final String startTime;

    @Schema(description = "End time of the appointment in yyyy-MM-dd HH:mm:ss format", example = "2025-08-15 15:00:00")
    private final String endTime;

    @Schema(description = "Unique identifier of the invitee", example = "3")
    private final String inviteeId;

    @Schema(description = "Full name of the invitee", example = "Akashdeep Singh")
    private final String inviteeName;

    @Schema(description = "Email address of the invitee", example = "akash.singh@gmail.com")
    private final String inviteeEmail;
}