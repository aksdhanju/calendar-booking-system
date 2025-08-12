package com.company.calendar.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class UpcomingAppointmentsResponseDto {
    @Schema(description = "Indicates if the request was successful")
    private final boolean success;

    @Schema(description = "Response message providing details about the result")
    private final String message;

    @Schema(description = "List of upcoming appointments")
    private final List<UpcomingAppointmentResponse> appointments;

    @Schema(description = "Current page number in the paginated response")
    private final int currentPage;

    @Schema(description = "Total number of pages available")
    private final int totalPages;

    @Schema(description = "Total number of appointments available")
    private final long totalItems;
}
