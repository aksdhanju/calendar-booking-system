package com.company.calendar.dto.appointment;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpcomingAppointmentsResponseDto {
    private boolean success;
    private String message;
    private List<UpcomingAppointmentResponse> appointments;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}
