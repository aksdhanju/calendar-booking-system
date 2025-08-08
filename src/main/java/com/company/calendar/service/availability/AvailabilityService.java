package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.dto.availability.UpdateAvailabilityRulesResult;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.exceptions.availability.AvailabilityRulesAlreadyExistsException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import com.company.calendar.service.user.UserService;
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
    private final UserService userService;

    public String createAvailabilityRules(AvailabilityRuleSetupRequest request) {
        var ownerId = request.getOwnerId();
        log.info("Creating availability rules for owner id: {}", ownerId);
        userService.validateUserExists(ownerId);

        log.debug("User validation passed for owner id: {}", ownerId);

        var mergedRules = availabilityServiceHelper.mergeOverlappingSlots(request.getRules());
        log.debug("Merged {} availability rules for owner id: {}", mergedRules.size(), ownerId);

        //compare and swap approach
        var rules = buildRules(ownerId, mergedRules);
        boolean saved = availabilityRuleRepository.saveIfAbsent(ownerId, rules);

        if (!saved) {
            log.warn("Availability rules already exist for ownerId: {}", ownerId);
            throw new AvailabilityRulesAlreadyExistsException(ownerId);
        }
        String message = "Availability rules created successfully for owner id: " + ownerId;
        log.info(message);
        return message;
    }

    public UpdateAvailabilityRulesResult updateAvailabilityRules(AvailabilityRuleSetupRequest request) {
        var ownerId = request.getOwnerId();
        log.info("Updating availability rules for owner id: {}", ownerId);
        userService.validateUserExists(ownerId);
        log.debug("User validation passed for owner id: {}", ownerId);

        var mergedRules = availabilityServiceHelper.mergeOverlappingSlots(request.getRules());
        log.debug("Merged {} availability rules for update", mergedRules.size());

        var alreadyCreated = true;
        if (availabilityRuleRepository.findByOwnerId(ownerId).isEmpty()) {
            log.warn("No existing availability rules found for owner id: {}, proceeding with creation.", ownerId);
            alreadyCreated = false;
        } else {
            log.debug("Existing availability rules found for owner id: {}, proceeding with update.", ownerId);
        }

        var rules = buildRules(ownerId, mergedRules);

        //no high contention here. Lost updates are fine here.
        //For the same owner, I am enabling latest update to be persisted in in-memory store
        availabilityRuleRepository.save(ownerId, rules);
        String message = (alreadyCreated ? "Availability rules updated successfully for owner id: "
                : "Availability rules created successfully for owner id: ") + ownerId;
        log.info(message);

        return UpdateAvailabilityRulesResult.builder()
                .created(alreadyCreated)
                .message(message)
                .build();
    }

    private List<AvailabilityRule> buildRules(String ownerId, List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules) {
        return rules.stream()
                .map(r -> {
                            log.debug("Building rule for owner id: {} on {} from {} to {}", ownerId, r.getDayOfWeek(), r.getStartTime(), r.getEndTime());
                            return AvailabilityRule.builder()
                                    .ownerId(ownerId)
                                    .dayOfWeek(r.getDayOfWeek())
                                    .startTime(r.getStartTime())
                                    .endTime(r.getEndTime())
                                    .build();
                        }
                ).collect(Collectors.toList());
    }

    public List<AvailableSlotDto> getAvailableSlots(String ownerId, LocalDate date) {
        log.info("Fetching available slots for owner id: {} on date: {}", ownerId, date);
        userService.validateUserExists(ownerId);
        log.debug("User validation passed for owner id: {}", ownerId);

        //available = total - booked
        var rules = availabilityServiceHelper.getRulesForOwnerAndDay(ownerId, date.getDayOfWeek());
        if (CollectionUtils.isEmpty(rules)) {
            log.warn("No availability rules found for owner id: {} on day: {}", ownerId, date.getDayOfWeek());
            return List.of();
        }
        log.debug("Found {} availability rules for owner id: {} on day: {}", rules.size(), ownerId, date.getDayOfWeek());

        var appointments = appointmentRepository.findByOwnerIdAndDate(ownerId, date);
        var bookedStartTimes = appointments.stream()
                .map(appt -> appt.getStartTime().toLocalTime())
                .collect(Collectors.toSet());

        log.debug("Found {} existing appointments for owner id: {} on {}", appointments.size(), ownerId, date);

        var availableSlots = availabilityServiceHelper.generateAvailableSlotsFromRules(rules, bookedStartTimes, date);
        log.info("Generated {} available slots for owner id: {} on {}", availableSlots.size(), ownerId, date);

        return availableSlots;
    }
}