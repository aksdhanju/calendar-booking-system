package com.company.calendar.validator;

import com.company.calendar.exceptions.InvalidStartDateTimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validator to ensure that:
 * 1. Appointment starts exactly at the top of the hour (e.g., 10:00:00.000).
 * 2. Appointment duration is exactly 60 minutes.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "appointment.time-validator-strategy", havingValue = "fullHour", matchIfMissing = true)
public class FullHourSixtyMinValidator implements AppointmentTimeValidator {
    @Override
    public void validate(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        log.debug("Validating appointment start: {}, end: {}", startDateTime, endDateTime);

        // Check if start time is exactly on the hour (minute, second, nano all 0)
        if (!(startDateTime.getMinute() == 0 &&
                startDateTime.getSecond() == 0 &&
                startDateTime.getNano() == 0)) {

            log.debug("Validation failed: Start time is not on the hour: {}", startDateTime);
            throw new InvalidStartDateTimeException("Appointments must start at the top of the hour and last 60 minutes");
        }

        // Check if the end time is exactly 60 minutes after the start time
        if (!endDateTime.minusMinutes(60).equals(startDateTime)) {
            log.debug("Validation failed: Appointment is not exactly 60 minutes. start: {}, end: {}", startDateTime, endDateTime);
            throw new InvalidStartDateTimeException("Appointment must be exactly 60 minutes long");
        }

        log.debug("Validation passed: Appointment duration and start time are valid.");
    }
}
