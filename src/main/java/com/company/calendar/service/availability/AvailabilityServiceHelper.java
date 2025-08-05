package com.company.calendar.service.availability;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
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
    private final AvailabilityRuleRepository availabilityRuleRepository;

    /**
     * Helper method to generate available slots from rules excluding already booked ones
     */
    public List<AvailableSlotDto> generateAvailableSlotsFromRules(List<AvailabilityRule> rules,
                                                                  Set<LocalTime> bookedStartTimes,
                                                                  LocalDate date) {
        List<AvailableSlotDto> availableSlots = new ArrayList<>();
        int duration = appointmentProperties.getDurationMinutes();

        for (AvailabilityRule rule : rules) {
            LocalTime slotStart = rule.getStartTime();
            LocalTime slotEnd = rule.getEndTime();

            while (true) {
                LocalTime slotEndTime = slotStart.plusMinutes(duration);

                if (!bookedStartTimes.contains(slotStart)) {
                    LocalDateTime startDateTime = LocalDateTime.of(date, slotStart);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(duration);

                    // Handle special case: if endTime is midnight and it's the last slot of day
                    if (slotEndTime.equals(LocalTime.MIDNIGHT)) {
                        endDateTime = LocalDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT);
                    }

                    availableSlots.add(AvailableSlotDto.builder()
                            .startDateTime(startDateTime)
                            .endDateTime(endDateTime)
                            .build());
                }

                boolean wrapsToNextDay = slotEndTime.isBefore(slotStart);
                if (wrapsToNextDay || slotEndTime.isAfter(slotEnd)) {
                    break;
                }

                slotStart = slotStart.plusMinutes(duration);
            }
        }
        return availableSlots;
    }

    public List<AvailabilityRule> getRulesForOwnerAndDay(String ownerId, LocalDate date) {
        var dayOfWeek = date.getDayOfWeek();
        return availabilityRuleRepository.findByOwnerIdAndDayOfWeek(ownerId, dayOfWeek);
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
