package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.UpdateAvailabilityRulesResult;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.exceptions.availability.AvailabilityRulesAlreadyExistsException;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import com.company.calendar.service.user.UserService;
import com.company.calendar.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityServiceHelper availabilityServiceHelper;
    private final UserService userService;

    public String createAvailabilityRules(AvailabilityRuleSetupRequest request) {
        var ownerId = request.getOwnerId();
        userService.validateUserExists(ownerId);
        //compare and swap approach
        var rules = buildRules(request);
        boolean saved = availabilityRuleRepository.saveIfAbsent(ownerId, rules);

        if (!saved) {
            throw new AvailabilityRulesAlreadyExistsException(ownerId);
        }
        return "Availability rules created successfully for owner id: " + ownerId;
    }

    public UpdateAvailabilityRulesResult updateAvailabilityRules(AvailabilityRuleSetupRequest request) {
        var ownerId = request.getOwnerId();
        userService.validateUserExists(ownerId);

        var created = true;
        if (availabilityRuleRepository.findByOwnerId(ownerId).isEmpty()) {
            log.warn("Availability Rules not found with owner id: {}", ownerId);
            created = false;
        }

        var rules = buildRules(request);

        //no high contention here. Lost updates are fine here?
        //For the same owner, I am enabling latest update to be persisted in in-memory store
        availabilityRuleRepository.save(ownerId, rules);
        var message = created ? "Availability rules updated successfully for owner id: "
                : "Availability rules created successfully for owner id: ";
        return UpdateAvailabilityRulesResult.builder()
                .created(created)
                .message(message + ownerId)
                .build();
    }

    private List<AvailabilityRule> buildRules(AvailabilityRuleSetupRequest request) {
        return request.getRules().stream()
                .map(r -> AvailabilityRule.builder()
                        .ownerId(request.getOwnerId())
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build()
                ).collect(Collectors.toList());
    }

    public List<Map<String, String>> getAvailableSlots(String ownerId, LocalDate date) {
        userService.validateUserExists(ownerId);
        //available = total - booked
        var rules = availabilityServiceHelper.getRulesForOwnerAndDay(ownerId, date);
        if (CollectionUtils.isEmpty(rules)) return List.of();

        var appointments = appointmentRepository.findByOwnerIdAndDate(ownerId, date);
        var bookedStartTimes = appointments.stream()
                .map(appt -> appt.getStartTime().toLocalTime())
                .collect(Collectors.toSet());

        return availabilityServiceHelper.generateAvailableSlotsFromRules(rules, bookedStartTimes, date)
                .stream()
                .map(slot -> Map.of(
                        "startDateTime", DateUtils.formatDateTime(slot.getStartDateTime()),
                        "endDateTime", DateUtils.formatDateTime(slot.getEndDateTime())
                ))
                .toList();
    }
}