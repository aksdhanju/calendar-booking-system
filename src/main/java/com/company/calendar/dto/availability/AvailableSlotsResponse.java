package com.company.calendar.dto.availability;

import lombok.Builder;
import java.util.List;

@Builder
public class AvailableSlotsResponse {
    private boolean success;
    private String message;
    private List<AvailableSlotDto> slots;
}
