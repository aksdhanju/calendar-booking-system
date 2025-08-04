package com.company.calendar.validator;

import java.time.LocalDateTime;

public interface AppointmentTimeValidator {
    void validate(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
