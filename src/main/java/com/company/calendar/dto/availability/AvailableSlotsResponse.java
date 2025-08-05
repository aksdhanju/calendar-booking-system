package com.company.calendar.dto.availability;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class AvailableSlotsResponse {
    private boolean success;
    private String message;
    private List<Map<String, String>> slots;
}
