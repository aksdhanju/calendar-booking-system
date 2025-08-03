package com.company.calendar.service.availability;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.exceptions.availability.AvailabilityRulesAlreadyExistsException;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import com.company.calendar.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentProperties appointmentProperties;
    private final UserService userService;

    public void createAvailabilityRules(AvailabilityRuleSetupRequest request) {
        var existingRules = availabilityRuleRepository.findByOwnerId(request.getOwnerId());
        if (!existingRules.isEmpty()) {
            throw new AvailabilityRulesAlreadyExistsException(request.getOwnerId());
        }
        buildAndSaveRules(request);
    }

    public void updateAvailabilityRules(AvailabilityRuleSetupRequest request) {
        buildAndSaveRules(request);
    }

    private void buildAndSaveRules(AvailabilityRuleSetupRequest request) {
        List<AvailabilityRule> rules = request.getRules().stream()
                .map(r -> AvailabilityRule.builder()
                        .ownerId(request.getOwnerId())
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .ruleType(r.getRuleType())
                        .build()
                ).collect(Collectors.toList());
        availabilityRuleRepository.save(request.getOwnerId(), rules);
    }

    public List<AvailableSlotDto> getAvailableSlots(String ownerId, LocalDate date) {
        if (userService.getUser(ownerId).isEmpty()) {
            throw new UserNotFoundException(ownerId);
        }
        //available = total - booked
        var dayOfWeek = date.getDayOfWeek();
        var rules = availabilityRuleRepository
                .findByOwnerIdAndDayOfWeekAndRuleType(ownerId, dayOfWeek, RuleType.AVAILABLE);

        if (CollectionUtils.isEmpty(rules)) return List.of();

        var appointments = appointmentRepository.findByOwnerIdAndDate(ownerId, date);
        var bookedStartTimes = appointments.stream()
                .map(appt -> appt.getStartTime().toLocalTime())
                .collect(Collectors.toSet());

        return generateAvailableSlotsFromRules(rules, bookedStartTimes, date);
    }

    private List<AvailableSlotDto> generateAvailableSlotsFromRules(
            List<AvailabilityRule> rules,
            Set<LocalTime> bookedStartTimes,
            LocalDate date
    ) {
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
                            .startTime(startDateTime)
                            .endTime(endDateTime)
                            .bookable(true)
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
}