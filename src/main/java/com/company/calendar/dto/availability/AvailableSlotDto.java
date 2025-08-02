package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AvailableSlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean bookable;
}
