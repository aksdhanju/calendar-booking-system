package com.company.calendar.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpcomingAppointmentsResponseDto {
    @Schema(description = "Indicates if the request was successful")
    private boolean success;

    @Schema(description = "Response message providing details about the result")
    private String message;

    @Schema(description = "List of upcoming appointments")
    private List<UpcomingAppointmentResponse> appointments;

    @Schema(description = "Current page number in the paginated response")
    private int currentPage;

    @Schema(description = "Total number of pages available")
    private int totalPages;

    @Schema(description = "Total number of appointments available")
    private long totalItems;
}
