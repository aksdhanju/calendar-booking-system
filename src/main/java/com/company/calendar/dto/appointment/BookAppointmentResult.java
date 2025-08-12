package com.company.calendar.dto.appointment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class BookAppointmentResult {
    private final String appointmentId;
    private final boolean newlyCreated;
    private final String message;
}
