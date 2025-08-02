package com.company.calendar.dto.availability;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AvailableSlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean bookable;
}
