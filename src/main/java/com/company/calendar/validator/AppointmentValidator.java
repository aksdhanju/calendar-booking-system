package com.company.calendar.validator;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.exceptions.appointment.AvailableSlotNotFoundException;
import com.company.calendar.exceptions.user.UserNotFoundException;
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
        if (userService.getUser(request.getOwnerId()).isEmpty()) {
            throw new UserNotFoundException(request.getOwnerId());
        }

        if (userService.getUser(request.getInviteeId()).isEmpty()) {
            throw new UserNotFoundException(request.getInviteeId());
        }

        var startDateTime = request.getStartDateTime();

        var endDateTime = startDateTime.plusMinutes(duration);
        appointmentTimeValidator.validate(startDateTime, endDateTime);

        //Extra check: check if we have a free slot as per this appointment
        var availabilityRules = availabilityServiceHelper.getRulesForOwnerAndDay(request.getOwnerId(), startDateTime.toLocalDate());
        if (CollectionUtils.isEmpty(availabilityRules)) {
            throw new AvailableSlotNotFoundException(DateUtils.formatDateTime(startDateTime), request.getOwnerId());
        }

        //Extra check: Please check test case 10
        var availableSlots =  availabilityServiceHelper.generateAvailableSlotsFromRules(availabilityRules, Set.of(), startDateTime.toLocalDate());
        var isAvailableSlotPresent = availableSlots.stream()
                .anyMatch(rule -> rule.getStartDateTime().equals(startDateTime));

        if (!isAvailableSlotPresent) {
            throw new AvailableSlotNotFoundException(DateUtils.formatDateTime(startDateTime), request.getOwnerId());
        }
        return true;
    }
}
