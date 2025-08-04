package com.company.calendar.validator;

import com.company.calendar.exceptions.InvalidStartDateTimeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "appointment.time-validator-strategy", havingValue = "fullHour", matchIfMissing = true)
public class FullHour60MinValidator implements AppointmentTimeValidator {
    @Override
    public void validate(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (!(startDateTime.getMinute() == 0 && startDateTime.getSecond() == 0 && startDateTime.getNano() == 0)) {
            throw new InvalidStartDateTimeException("Appointments must start at the top of the hour and last 60 minutes.");
        }
        // Validate that end time is exactly start time + duration
        if (!endDateTime.minusMinutes(60).equals(startDateTime)) {
            throw new InvalidStartDateTimeException("Appointment must be exactly " + 60 + " minutes long.");
        }
    }
}
