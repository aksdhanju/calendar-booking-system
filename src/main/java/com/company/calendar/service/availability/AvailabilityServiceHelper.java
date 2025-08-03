package com.company.calendar.service.availability;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AvailabilityServiceHelper {
    private final AppointmentProperties appointmentProperties;

    /**
     * Helper method to generate available slots from rules excluding already booked ones
     */
    public List<AvailableSlotDto> generateAvailableSlotsFromRules(List<AvailabilityRule> rules, Set<LocalTime> bookedStartTimes, LocalDate date) {
        List<AvailableSlotDto> availableSlots = new ArrayList<>();
        int duration = appointmentProperties.getDurationMinutes();

        for (AvailabilityRule rule : rules) {
            LocalTime slotStart = rule.getStartTime();
            LocalTime slotEnd = rule.getEndTime();

            while (!slotStart.plusMinutes(duration).isAfter(slotEnd)) {
                if (!bookedStartTimes.contains(slotStart)) {
                    LocalDateTime startDateTime = LocalDateTime.of(date, slotStart);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(duration);

                    availableSlots.add(AvailableSlotDto.builder()
                            .startTime(startDateTime)
                            .endTime(endDateTime)
                            .bookable(true)
                            .build());
                }
                slotStart = slotStart.plusMinutes(duration);
            }
        }
        return availableSlots;
    }

    public List<AvailabilityRule> buildRules(AvailabilityRuleSetupRequest request) {
        return request.getRules().stream()
                .map(r -> AvailabilityRule.builder()
                        .ownerId(request.getOwnerId())
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build()
                ).collect(Collectors.toList());
    }
}
