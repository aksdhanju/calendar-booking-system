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
@ConditionalOnProperty(name = "appointment.booking-strategy", havingValue = "pessimistic")
public class PessimisticBookingStrategy implements AppointmentBookingStrategy {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentOwnerLockManager appointmentOwnerLockManager;

    @Override
    public boolean book(BookAppointmentRequest request, int durationMinutes, String appointmentId) {
        LocalDateTime startTime = request.getStartDateTime();
        Object ownerLock = appointmentOwnerLockManager.getLock(request.getOwnerId());
        //not doing 2 times validation here that slot in request is free or not. We are assuming it will be free.
        //if it wont be, means some one else has booked it in meanwhile/concurrently, then slotFree will come as false
        //and we will exit.

        synchronized (ownerLock) {
            try {
                boolean alreadyBooked = appointmentRepository.existsByOwnerIdAndStartTime(request.getOwnerId(), startTime);
                if (alreadyBooked) {
                    return false;
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
            } finally {
                appointmentOwnerLockManager.releaseLock(request.getOwnerId());
            }
        }
    }
}
