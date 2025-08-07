package com.company.calendar.service.appointment;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.entity.Appointment;
import com.company.calendar.repository.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "appointment.booking-strategy", havingValue = "optimistic", matchIfMissing = true)
public class OptimisticBookingStrategy implements AppointmentBookingStrategy {

    private final AppointmentRepository appointmentRepository;

    @Override
    public boolean book(BookAppointmentRequest request, int durationMinutes, String appointmentId) {
        var startTime = request.getStartDateTime();
        var endTime = startTime.plusMinutes(durationMinutes);

        log.info("Attempting optimistic booking for appointmentId: {}, ownerId: {}, inviteeId: {}, startTime: {}, endTime: {}",
                appointmentId, request.getOwnerId(), request.getInviteeId(), startTime, endTime);

        var appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .ownerId(request.getOwnerId())
                .inviteeId(request.getInviteeId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        var booked = appointmentRepository.saveIfSlotFree(appointment);

        if (booked) {
            log.info("Appointment booked successfully for appointmentId: {}", appointmentId);
        } else {
            log.warn("Failed to book appointment for appointmentId: {}. Slot might be already taken.", appointmentId);
        }

        return booked;
    }
}
