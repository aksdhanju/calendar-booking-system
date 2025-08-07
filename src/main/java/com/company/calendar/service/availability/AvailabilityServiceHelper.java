package com.company.calendar.service.availability;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import com.company.calendar.utils.DateUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public List<AvailableSlotDto> generateAvailableSlotsFromRules(@NotEmpty List<AvailabilityRule> rules,
                                                                  Set<LocalTime> bookedStartTimes,
                                                                  @NotNull LocalDate date) {
        //rules for particular date and hence day
        //bookedStartTimes would be a subset of times in rules
        //only consider rules which has day same as that of date
        List<AvailableSlotDto> availableSlots = new ArrayList<>();
        int duration = appointmentProperties.getDurationMinutes();

        for (AvailabilityRule rule : rules) {

            if (!rule.getDayOfWeek().equals(date.getDayOfWeek())) continue;

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
                            .startDateTime(DateUtils.formatDateTime(startDateTime))
                            .endDateTime(DateUtils.formatDateTime(endDateTime))
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

    public List<AvailabilityRule> getRulesForOwnerAndDay(String ownerId, DayOfWeek dayOfWeek) {
        return availabilityRuleRepository.findByOwnerIdAndDayOfWeek(ownerId, dayOfWeek);
    }

    public List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> mergeOverlappingSlots(List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules) {
        // Step 1: Group by DayOfWeek
        Map<DayOfWeek, List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest>> rulesByDay = rules.stream()
                .collect(Collectors.groupingBy(AvailabilityRuleSetupRequest.AvailabilityRuleRequest::getDayOfWeek));


        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> mergedRules = new ArrayList<>();

        // Step 2: For each day, sort and merge intervals
        for (Map.Entry<DayOfWeek, List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest>> entry : rulesByDay.entrySet()) {
            var dayRules = entry.getValue();
            if (dayRules.size() == 1) {
                mergedRules.add(dayRules.getFirst());
                continue;
            }

            // Sort by startTime
            dayRules.sort(Comparator.comparing(AvailabilityRuleSetupRequest.AvailabilityRuleRequest::getStartTime));

            List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> mergedDayRules = new ArrayList<>();

            AvailabilityRuleSetupRequest.AvailabilityRuleRequest current = dayRules.getFirst();

            for (int i = 1; i < dayRules.size(); i++) {
                AvailabilityRuleSetupRequest.AvailabilityRuleRequest next = dayRules.get(i);

                // If overlapping or adjacent
                if (!current.getEndTime().isBefore(next.getStartTime())) {
                    // Merge intervals
                    current = AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                            .dayOfWeek(current.getDayOfWeek())
                            .startTime(current.getStartTime())
                            .endTime(max(current.getEndTime(), next.getEndTime()))
                            .build();
                } else {
                    mergedDayRules.add(current);
                    current = next;
                }
            }

            mergedDayRules.add(current); // Add the last interval
            mergedRules.addAll(mergedDayRules);
        }
        return mergedRules;
    }

    private LocalTime max(LocalTime t1, LocalTime t2) {
        return t1.isAfter(t2) ? t1 : t2;
    }
}
