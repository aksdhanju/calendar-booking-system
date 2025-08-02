package com.company.calendar.service.appointment;

import com.company.calendar.dto.BookAppointmentRequest;

public interface AppointmentBookingStrategy {
    String book(BookAppointmentRequest request, int durationMinutes);
}
