package com.company.calendar.validator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "appointment.time-validator-strategy", havingValue = "fullHour", matchIfMissing = true)
public class FullHour60MinValidator implements AppointmentTimeValidator {
    @Override
    public void validate(LocalDateTime startTime, LocalDateTime endTime) {
        if (!(startTime.getMinute() == 0 && startTime.getSecond() == 0 && startTime.getNano() == 0)) {
            throw new IllegalArgumentException("Appointments must start at the top of the hour and last 60 minutes.");
        }
        // Validate that end time is exactly start time + duration
        if (!endTime.minusMinutes(60).equals(startTime)) {
            throw new IllegalArgumentException("Appointment must be exactly " + 60 + " minutes long.");
        }
    }
}
