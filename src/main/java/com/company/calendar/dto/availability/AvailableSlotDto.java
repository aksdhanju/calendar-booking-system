package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableSlotDto {
    private String startDateTime;
    private String endDateTime;
}
