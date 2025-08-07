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
@ConditionalOnProperty(name = "appointment.booking-strategy", havingValue = "pessimistic")
public class PessimisticBookingStrategy implements AppointmentBookingStrategy {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentOwnerLockManager appointmentOwnerLockManager;

    @Override
    public boolean book(BookAppointmentRequest request, int durationMinutes, String appointmentId) {
        var startTime = request.getStartDateTime();
        var ownerId = request.getOwnerId();
        log.info("Attempting to book appointment for ownerId: {}, startTime: {}, appointmentId: {}", ownerId, startTime, appointmentId);

        Object ownerLock = appointmentOwnerLockManager.getLock(ownerId);
        //not doing 2 times validation here that slot in request is free or not. We are assuming it will be free.
        //if it wont be, means some one else has booked it in meanwhile/concurrently, then slotFree will come as false
        //and we will exit.

        synchronized (ownerLock) {
            try {
                var alreadyBooked = appointmentRepository.existsByOwnerIdAndStartTime(ownerId, startTime);
                if (alreadyBooked) {
                    log.warn("Appointment slot already booked for ownerId: {}, startTime: {}", ownerId, startTime);
                    return false;
                }

                var appointment = Appointment.builder()
                        .appointmentId(appointmentId)
                        .ownerId(ownerId)
                        .inviteeId(request.getInviteeId())
                        .startTime(startTime)
                        .endTime(startTime.plusMinutes(durationMinutes))
                        .build();

                log.info("Appointment successfully booked: appointmentId: {}, ownerId: {}, startTime: {}, endTime: {}",
                        appointmentId, ownerId, startTime, startTime.plusMinutes(durationMinutes));
                appointmentRepository.save(appointment);
                return true;
            } catch (Exception e) {
                log.error("Error while booking appointment for ownerId: {}, startTime: {}, error: {}", ownerId, startTime, e.getMessage(), e);
                throw e;
            } finally {
                appointmentOwnerLockManager.releaseLock(ownerId);
                log.debug("Lock released for ownerId: {}", ownerId);
            }
        }
    }
}
