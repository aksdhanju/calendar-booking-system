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
@ConditionalOnProperty(name = "appointment.booking-strategy", havingValue = "optimistic", matchIfMissing = true)
public class OptimisticBookingStrategy implements AppointmentBookingStrategy {

    private final AppointmentRepository appointmentRepository;

    @Override
    public String book(BookAppointmentRequest request, int durationMinutes) {
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        Appointment appointment = Appointment.builder()
                .appointmentId(UUID.randomUUID().toString())
                .ownerId(request.getOwnerId())
                .inviteeId(request.getInviteeId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        boolean success = appointmentRepository.saveIfSlotFree(appointment);
        if (!success) {
            throw new IllegalStateException("This time slot is already booked.");
        }
        return appointment.getAppointmentId();
    }
}
