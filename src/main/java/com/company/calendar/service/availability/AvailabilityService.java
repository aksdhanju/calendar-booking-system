package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.exceptions.availability.AvailabilityRulesAlreadyExistsException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityServiceHelper availabilityServiceHelper;

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
        List<AvailabilityRule> rules = availabilityServiceHelper.buildRules(request);
        availabilityRuleRepository.save(request.getOwnerId(), rules);
    }

    public List<AvailableSlotDto> getAvailableSlots(String ownerId, LocalDate date) {
        //available = total - booked
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