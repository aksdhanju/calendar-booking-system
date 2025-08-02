package com.company.calendar.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpcomingAppointmentResponse {
    private String appointmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String inviteeId;
    private String inviteeName;
    private String inviteeEmail;
}
