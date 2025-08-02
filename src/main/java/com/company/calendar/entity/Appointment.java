package com.company.calendar.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Appointment {
    private String appointmentId;
    private String ownerId;
    private String inviteeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
