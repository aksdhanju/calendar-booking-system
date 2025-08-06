package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResult;
import com.company.calendar.dto.appointment.UpcomingAppointmentResponse;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.entity.User;
import com.company.calendar.entity.UserMetadata;
import com.company.calendar.exceptions.appointment.SlotAlreadyBookedException;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.service.user.UserService;
import com.company.calendar.utils.AppointmentServiceUtil;
import com.company.calendar.utils.DateUtils;
import com.company.calendar.validator.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

import static com.company.calendar.utils.AppointmentServiceUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentProperties appointmentProperties;
    private final AppointmentBookingStrategy appointmentBookingStrategy;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentIdempotencyStore appointmentIdempotencyStore;
    private final AppointmentLockManager appointmentLockManager;
    private final AppointmentValidator appointmentValidator;
    private final UserService userService;

    // Book an appointment if it's not already taken
    public BookAppointmentResult bookAppointment(String idempotencyKey, BookAppointmentRequest request) {
        // Fast path – return if already present
        var ownerId = request.getOwnerId();
        var existing = appointmentIdempotencyStore.get(idempotencyKey);
        if (existing != null)
            return BookAppointmentResult.builder()
                    .appointmentId(existing)
                    .message("Appointment already exists for owner id: " + ownerId)
                    .newlyCreated(false).build();

        // Get or create a lock per idempotency key
        var lock = appointmentLockManager.getLock(idempotencyKey);

        synchronized (lock) {
            try {
                // Recheck after acquiring lock (double-checked locking)
                existing = appointmentIdempotencyStore.get(idempotencyKey);
                if (existing != null)
                    return BookAppointmentResult.builder()
                            .appointmentId(existing)
                            .newlyCreated(false)
                            .message("Appointment already exists for owner id: " + ownerId)
                            .build();

                var duration = appointmentProperties.getDurationMinutes();
                appointmentValidator.validateAppointment(request, duration);

                var appointmentId = UUID.randomUUID().toString();
                var success = appointmentBookingStrategy.book(request, duration, appointmentId);
                if (!success) {
                    log.info("Slot already booked, not saving appointment for key: {}", ownerId);
                    throw new SlotAlreadyBookedException(request.getOwnerId()); // Booking failed
                }

                appointmentIdempotencyStore.put(idempotencyKey, appointmentId);
                return BookAppointmentResult.builder()
                        .appointmentId(appointmentId)
                        .newlyCreated(true)
                        .message("Appointment booked successfully for owner id: " + ownerId)
                        .build();
            } finally {
                // clean up lockMap explicitly to avoid memory bloat
                appointmentLockManager.releaseLock(idempotencyKey);
            }
        }
    }

    public UpcomingAppointmentsResponseDto getUpcomingAppointments(String ownerId, int page, int size) {
        userService.validateUserExists(ownerId);
        var now = LocalDateTime.now();
        var pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        var pagedAppointments = appointmentRepository.findByOwnerIdAndStartTimeAfter(ownerId, now, pageable);

        if (pagedAppointments.isEmpty()) {
            return emptyResponse(ownerId, page);
        }
        // N+1 Query Problem
        // Inside the .map(), we are calling userService.getUser(inviteeId) for every appointment.
        // If there are 100 appointments, this will make 100 DB/API calls.

        // Batch fetch invitees
        var inviteeIds = pagedAppointments.stream()
                .map(Appointment::getInviteeId)
                .collect(Collectors.toSet());

        var inviteeMap = userService.getUsersByIds(inviteeIds); // Map<String, User>

        var upcomingAppointments = pagedAppointments.stream()
                .map(appointment -> toResponse(appointment, inviteeMap))
                .toList();

        return successResponse(ownerId, pagedAppointments, upcomingAppointments);
    }
}
