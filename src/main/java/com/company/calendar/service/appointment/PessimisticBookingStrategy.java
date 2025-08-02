package com.company.calendar.service.appointment;

import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.entity.Appointment;
import com.company.calendar.repository.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "appointment.booking-strategy", havingValue = "pessimistic")
public class PessimisticBookingStrategy implements AppointmentBookingStrategy{
    private final AppointmentRepository appointmentRepository;

    @Override
    public boolean book(BookAppointmentRequest request, int durationMinutes, String appointmentId) {
        if (appointmentRepository.existsById(appointmentId)) {
            return false; // Already booked with same ID, idempotent response
        }

        LocalDateTime startTime = request.getStartTime();

        synchronized (this) {
            boolean slotFree = appointmentRepository.existsByOwnerIdAndStartTime(request.getOwnerId(), startTime);
            if (!slotFree) {
                throw new IllegalStateException("This time slot is already booked.");
            }

            Appointment appointment = Appointment.builder()
                    .appointmentId(appointmentId)
                    .ownerId(request.getOwnerId())
                    .inviteeId(request.getInviteeId())
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(durationMinutes))
                    .build();

            appointmentRepository.save(appointment);
            return true;
        }
    }
}
