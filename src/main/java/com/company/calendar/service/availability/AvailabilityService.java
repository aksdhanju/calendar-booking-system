package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;
import com.company.calendar.dto.availability.AvailabilitySetupRequest;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityServiceHelper availabilityServiceHelper;

    // POST — create only if rules don't already exist
    public void createAvailabilityRules(AvailabilitySetupRequest request) {
        List<AvailabilityRule> existingRules = availabilityRuleRepository.findByOwnerId(request.getOwnerId());
        if (!existingRules.isEmpty()) {
            throw new IllegalStateException("Availability rules already exist for owner: " + request.getOwnerId());
        }

        List<AvailabilityRule> rules = availabilityServiceHelper.buildRules(request);
        availabilityRuleRepository.create(request.getOwnerId(), rules);
    }

    // PUT — overwrite all existing rules (idempotent)
    public void updateAvailabilityRules(AvailabilitySetupRequest request) {
        List<AvailabilityRule> rules = availabilityServiceHelper.buildRules(request);
        availabilityRuleRepository.upsert(request.getOwnerId(), rules);
    }

    public List<AvailableSlotDto> getAvailableSlots(String ownerId, LocalDate date) {
        var dayOfWeek = date.getDayOfWeek();
        var rules = availabilityRuleRepository
                .findByOwnerIdAndDayOfWeekAndRuleType(ownerId, dayOfWeek, RuleType.AVAILABLE);

        if (CollectionUtils.isEmpty(rules)) return List.of();

        var appointments = appointmentRepository.findByOwnerIdAndDate(ownerId, date);
        var bookedStartTimes = appointments.stream()
                .map(appt -> appt.getStartTime().toLocalTime())
                .collect(Collectors.toSet());

        return availabilityServiceHelper.generateAvailableSlotsFromRules(rules, bookedStartTimes, date);
    }
}