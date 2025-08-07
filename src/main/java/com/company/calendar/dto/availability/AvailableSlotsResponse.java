package com.company.calendar.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@Schema(description = "Response containing available time slots for an owner on a particular date")
public class AvailableSlotsResponse {
    @Schema(description = "Indicates if the request was successful")
    private boolean success;
    @Schema(description = "Descriptive message about the response")
    private String message;
    @Schema(description = "List of available slots with start and end time")
    private List<AvailableSlotDto> slots;
}
