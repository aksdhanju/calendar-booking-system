package com.company.calendar.service.availability;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailableSlotDto;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityServiceHelper availabilityServiceHelper;
    private final UserService userService;
    private final Map<String, Object> ownerLocks = new ConcurrentHashMap<>();


    public void createAvailabilityRules(AvailabilityRuleSetupRequest request) {
        if (userService.getUser(request.getOwnerId()).isEmpty()) {
            throw new UserNotFoundException(request.getOwnerId());
        }
        //compare and swap approach
        var rules = buildRules(request);
        boolean saved = availabilityRuleRepository.saveIfAbsent(request.getOwnerId(), rules);

        if (!saved) {
            throw new AvailabilityRulesAlreadyExistsException(request.getOwnerId());
        }
    }

    public void updateAvailabilityRules(AvailabilityRuleSetupRequest request) {
        Object lock = ownerLocks.computeIfAbsent(request.getOwnerId(), id -> new Object());
        //synchronized approach
        //no high contention here. Its fine to take a lock per owner here. Pros/cons
        synchronized (lock) {
            try {
                if (userService.getUser(request.getOwnerId()).isEmpty()) {
                    throw new UserNotFoundException(request.getOwnerId());
                }
                var rules = buildRules(request);
                availabilityRuleRepository.save(request.getOwnerId(), rules);
            } finally {
                ownerLocks.remove(request.getOwnerId(), lock);
            }
        }
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
        if (userService.getUser(ownerId).isEmpty()) {
            throw new UserNotFoundException(ownerId);
        }
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