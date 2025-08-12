package com.company.calendar.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@Schema(description = "Response containing available time slots for an owner on a particular date")
public final class AvailableSlotsResponse {
    @Schema(description = "Indicates if the request was successful", example = "true")
    private final boolean success;
    @Schema(description = "Descriptive message about the response", example = "Available slots fetched successfully for owner id: 1")
    private final String message;
    @Schema(
            description = "List of available slots with start and end time",
            example = "[{\"startDateTime\":\"2025-08-08 10:00:00\",\"endDateTime\":\"2025-08-08 11:00:00\"}]"
    )
    private final List<AvailableSlotDto> slots;
}
