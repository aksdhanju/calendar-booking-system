package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AvailableSlotsResponse {
    private boolean success;
    private String message;
    private List<AvailableSlotDto> slots;
}
