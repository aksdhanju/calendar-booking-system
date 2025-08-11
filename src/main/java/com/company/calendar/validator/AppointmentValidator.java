package com.company.calendar.validator;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.exceptions.appointment.AvailableSlotNotFoundException;
import com.company.calendar.service.availability.AvailabilityServiceHelper;
import com.company.calendar.service.user.UserService;
import com.company.calendar.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentValidator {
    private final AvailabilityServiceHelper availabilityServiceHelper;
    private final UserService userService;
    private final AppointmentTimeValidator appointmentTimeValidator;

    public boolean validateAppointment(BookAppointmentRequest request, long duration) {
        log.debug("Validating appointment request: {} with duration: {} minutes", request, duration);

        userService.validateUserExists(request.getOwnerId());
        userService.validateUserExists(request.getInviteeId());

        log.debug("Owner and invitee exist for owner id: {} and invitee id: {}", request.getOwnerId(), request.getInviteeId());

        var startDateTime = request.getStartDateTime();
        var endDateTime = startDateTime.plusMinutes(duration);

        log.debug("Validating appointment time from {} to {}", startDateTime, endDateTime);

        appointmentTimeValidator.validate(startDateTime, endDateTime);

        var availabilityRules = availabilityServiceHelper.getRulesForOwnerAndDay(request.getOwnerId(), startDateTime.toLocalDate().getDayOfWeek());
        if (CollectionUtils.isEmpty(availabilityRules)) {
            log.warn("No availability rules found for owner id: {} on {}", request.getOwnerId(), startDateTime.getDayOfWeek());
            throw new AvailableSlotNotFoundException(DateUtils.formatDateTime(startDateTime), request.getOwnerId());
        }

        var availableSlots =  availabilityServiceHelper.generateAvailableSlotsFromRules(availabilityRules, Set.of(), startDateTime.toLocalDate());
        var isAvailableSlotPresent = availableSlots.stream()
                .anyMatch(rule -> rule.getStartDateTime().equals(DateUtils.formatDateTime(startDateTime)));

        if (!isAvailableSlotPresent) {
            log.warn("No available slot found for appointment start date time: {} for owner id: {}", startDateTime, request.getOwnerId());
            throw new AvailableSlotNotFoundException(DateUtils.formatDateTime(startDateTime), request.getOwnerId());
        }
        log.debug("Appointment validation successful for start date time: {} and ownerId: {}, inviteeId: {}", startDateTime, request.getOwnerId(), request.getInviteeId());
        return true;
    }
}
