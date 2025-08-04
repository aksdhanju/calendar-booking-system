package com.company.calendar.dto.appointment;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookAppointmentResponseDto {
    private boolean success;
    private String message;
    private String appointmentId;
    private String errorCode;
}
