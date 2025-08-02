package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.validator.AppointmentTimeValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentTimeValidator appointmentTimeValidator;
    private final AppointmentProperties appointmentProperties;
    private final AppointmentBookingStrategy appointmentBookingStrategy;

    // Book an appointment if it's not already taken
    public String bookAppointment(@Valid BookAppointmentRequest request) {
        var startTime = request.getStartTime();
        var duration = appointmentProperties.getDurationMinutes();
        var endTime = startTime.plusMinutes(duration);
        appointmentTimeValidator.validate(startTime, endTime);
        return appointmentBookingStrategy.book(request, duration);
    }
}
