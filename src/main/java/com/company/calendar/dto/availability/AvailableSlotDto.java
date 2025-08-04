package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AvailableSlotDto {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean bookable;
}
