package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.validator.AppointmentTimeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    private final AppointmentTimeValidator appointmentTimeValidator;
    private final AppointmentProperties appointmentProperties;
    private final AppointmentBookingStrategy appointmentBookingStrategy;

    // Book an appointment if it's not already taken
    public boolean bookAppointment(String appointmentId, BookAppointmentRequest request) {
        var startTime = request.getStartTime();
        var duration = appointmentProperties.getDurationMinutes();
        var endTime = startTime.plusMinutes(duration);
        appointmentTimeValidator.validate(startTime, endTime);
        log.info("{}: appointment time validation passed", appointmentId);
        return appointmentBookingStrategy.book(request, duration, appointmentId);
    }
}
