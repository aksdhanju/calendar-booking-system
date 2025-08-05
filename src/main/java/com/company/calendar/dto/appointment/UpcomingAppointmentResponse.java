package com.company.calendar.dto.appointment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpcomingAppointmentResponse {
    private String appointmentId;
    private String startTime;
    private String endTime;
    private String inviteeId;
    private String inviteeName;
    private String inviteeEmail;
}
