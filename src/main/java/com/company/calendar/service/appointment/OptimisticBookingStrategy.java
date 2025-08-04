package com.company.calendar.service.appointment;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.entity.Appointment;
import com.company.calendar.repository.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "appointment.booking-strategy", havingValue = "optimistic", matchIfMissing = true)
public class OptimisticBookingStrategy implements AppointmentBookingStrategy {

    private final AppointmentRepository appointmentRepository;

    @Override
    public boolean book(BookAppointmentRequest request, int durationMinutes, String appointmentId) {
        LocalDateTime startTime = request.getStartDateTime();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .ownerId(request.getOwnerId())
                .inviteeId(request.getInviteeId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return appointmentRepository.saveIfSlotFree(appointment);
    }
}
