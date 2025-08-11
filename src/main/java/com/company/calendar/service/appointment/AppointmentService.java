package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResult;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.exceptions.appointment.SlotAlreadyBookedException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.service.user.UserService;
import com.company.calendar.validator.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.company.calendar.utils.AppointmentServiceUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentProperties appointmentProperties;
    private final AppointmentBookingStrategy appointmentBookingStrategy;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentIdempotencyStore appointmentIdempotencyStore;
    private final AppointmentIdempotencyLockManager appointmentIdempotencyLockManager;
    private final AppointmentValidator appointmentValidator;
    private final UserService userService;

    public BookAppointmentResult bookAppointment(String idempotencyKey, BookAppointmentRequest request) {
        var ownerId = request.getOwnerId();
        log.info("Received appointment booking request for ownerId: {}, idempotencyKey: {}", ownerId, idempotencyKey);

        var existing = appointmentIdempotencyStore.get(idempotencyKey);
        if (existing != null) {
            log.info("Returning cached appointment for idempotencyKey: {}, appointmentId: {}", idempotencyKey, existing);
            return BookAppointmentResult.builder()
                    .appointmentId(existing)
                    .message("Appointment already exists for owner id: " + ownerId)
                    .newlyCreated(false).build();
        }

        var lock = appointmentIdempotencyLockManager.getLock(idempotencyKey);

        synchronized (lock) {
            try {
                existing = appointmentIdempotencyStore.get(idempotencyKey);
                if (existing != null) {
                    log.info("Returning cached appointment (post-lock) for idempotencyKey: {}, appointmentId: {}", idempotencyKey, existing);
                    return BookAppointmentResult.builder()
                            .appointmentId(existing)
                            .newlyCreated(false)
                            .message("Appointment already exists for owner id: " + ownerId)
                            .build();
                }

                var duration = appointmentProperties.getDurationMinutes();
                log.debug("Validating appointment for ownerId: {}, duration: {} minutes", ownerId, duration);
                appointmentValidator.validateAppointment(request, duration);

                var appointmentId = UUID.randomUUID().toString();
                log.info("Generated new appointmentId: {} for ownerId: {}, inviteeId: {}", appointmentId, ownerId, request.getInviteeId());

                var success = appointmentBookingStrategy.book(request, duration, appointmentId);
                if (!success) {
                    log.warn("Slot already booked for ownerId: {}, startTime: {}", ownerId, request.getStartDateTime());
                    throw new SlotAlreadyBookedException(ownerId);
                }

                appointmentIdempotencyStore.put(idempotencyKey, appointmentId);
                log.info("Successfully booked appointment. Stored in idempotency store for key: {}", idempotencyKey);

                return BookAppointmentResult.builder()
                        .appointmentId(appointmentId)
                        .newlyCreated(true)
                        .message("Appointment booked successfully for owner id: " + ownerId)
                        .build();
            } catch (Exception e) {
                log.error("Error while booking appointment for ownerId: {}, idempotencyKey: {}, error: {}", ownerId, idempotencyKey, e.getMessage(), e);
                throw e;
            } finally {
                appointmentIdempotencyLockManager.releaseLock(idempotencyKey);
                log.debug("Released lock for idempotencyKey: {}", idempotencyKey);
            }
        }
    }

    public UpcomingAppointmentsResponseDto getUpcomingAppointments(String ownerId, int page, int size) {
        log.info("Fetching upcoming appointments for ownerId: {}, page: {}, size: {}", ownerId, page, size);

        userService.validateUserExists(ownerId);
        var now = LocalDateTime.now();
        var pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        var pagedAppointments = appointmentRepository.findByOwnerIdAndStartTimeAfter(ownerId, now, pageable);

        if (pagedAppointments.isEmpty()) {
            log.info("No upcoming appointments found for ownerId: {}", ownerId);
            return emptyResponse(ownerId, page);
        }

        var inviteeIds = pagedAppointments.stream()
                .map(Appointment::getInviteeId)
                .collect(Collectors.toSet());

        log.debug("Fetching user details for invitees: {}", inviteeIds);
        var inviteeMap = userService.getUsersByIds(inviteeIds);

        var upcomingAppointments = pagedAppointments.stream()
                .map(appointment -> toResponse(appointment, inviteeMap))
                .toList();

        log.info("Returning {} upcoming appointments for ownerId: {}", upcomingAppointments.size(), ownerId);
        return successResponse(ownerId, pagedAppointments, upcomingAppointments);
    }
}
