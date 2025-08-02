package com.company.calendar.service;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;
import com.company.calendar.dto.availability.AvailabilitySetupRequest;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentProperties appointmentProperties;

    // POST — create only if rules don't already exist
    public void createAvailabilityRules(AvailabilitySetupRequest request) {
        List<AvailabilityRule> existingRules = availabilityRuleRepository.findByOwnerId(request.getOwnerId());
        if (!existingRules.isEmpty()) {
            throw new IllegalStateException("Availability rules already exist for owner: " + request.getOwnerId());
        }

        List<AvailabilityRule> rules = buildRules(request);
        availabilityRuleRepository.create(request.getOwnerId(), rules);
    }

    // PUT — overwrite all existing rules (idempotent)
    public void updateAvailabilityRules(AvailabilitySetupRequest request) {
        List<AvailabilityRule> rules = buildRules(request);
        availabilityRuleRepository.upsert(request.getOwnerId(), rules);
    }

    private List<AvailabilityRule> buildRules(AvailabilitySetupRequest request) {
        return request.getRules().stream()
                .map(r -> AvailabilityRule.builder()
                        .ownerId(request.getOwnerId())
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .ruleType(r.getRuleType())
                        .build()
                ).collect(Collectors.toList());
    }

    public List<AvailableSlotDto> getAvailableSlots(@NotBlank String ownerId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        //NOTE: no specific logic for a particular date. We get day of week from day
        //In future, date can be added in AvailabilityRule class

        // 1. Get rules for that day
        List<AvailabilityRule> rules = availabilityRuleRepository
                .findByOwnerIdAndDayOfWeekAndRuleType(ownerId, dayOfWeek, RuleType.AVAILABLE);
        if (rules == null || rules.isEmpty()) return Collections.emptyList();

        // 2. Fetch existing appointments for the day
        List<Appointment> appointments = appointmentRepository.findByOwnerIdAndDate(ownerId, date);

        // Convert appointments to blocked/booked slots
        Set<LocalTime> bookedStartTimes = appointments.stream()
                .map(appt -> appt.getStartTime().toLocalTime())
                .collect(Collectors.toSet());

        List<AvailableSlotDto> availableSlots = new ArrayList<>();

        //availableSlots = Total Slots - Booked Slots
        var duration = appointmentProperties.getDurationMinutes();
        for (AvailabilityRule rule : rules) {
            LocalTime slotStart = rule.getStartTime();
            LocalTime slotEnd = rule.getEndTime();

            while (!slotStart.plusMinutes(duration).isAfter(slotEnd)) {
                // Is this slot already booked?
                if (!bookedStartTimes.contains(slotStart)) {
                    LocalDateTime startDateTime = LocalDateTime.of(date, slotStart);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(duration);

                    availableSlots.add(new AvailableSlotDto(startDateTime, endDateTime, true));
                }

                slotStart = slotStart.plusMinutes(duration); // Move to next aligned 60-min slot
            }
        }
        return availableSlots;
    }
}
