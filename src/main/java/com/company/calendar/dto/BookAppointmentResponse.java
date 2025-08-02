package com.company.calendar.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookAppointmentResponse {
    private boolean success;
    private String message;
    private String appointmentId;
    private String errorCode;
}
