package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class AvailableSlotDto {
    private final String startDateTime;
    private final String endDateTime;
}
