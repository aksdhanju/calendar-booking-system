package com.company.calendar.dto.appointment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookAppointmentResult {
    private String appointmentId;
    private boolean newlyCreated;
}
