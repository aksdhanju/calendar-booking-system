package com.company.calendar.service.appointment;

import com.company.calendar.dto.appointment.BookAppointmentRequest;

public interface AppointmentBookingStrategy {
    boolean book(BookAppointmentRequest request, int durationMinutes, String appointmentId);
}
