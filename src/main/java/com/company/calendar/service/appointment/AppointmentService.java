package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResult;
import com.company.calendar.dto.appointment.UpcomingAppointmentResponse;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.exceptions.appointment.SlotAlreadyBookedException;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.service.user.UserService;
import com.company.calendar.utils.DateUtils;
import com.company.calendar.validator.AppointmentValidator;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentProperties appointmentProperties;
    private final AppointmentBookingStrategy appointmentBookingStrategy;
    private final AppointmentRepository appointmentRepository;
    private final Cache<String, String> appointmentIdempotencyStore;
    private final Cache<String, Object> appointmentLockMap;
    private final AppointmentValidator appointmentValidator;
    private final UserService userService;
    private final Clock clock;

    // Book an appointment if it's not already taken
    public BookAppointmentResult bookAppointment(String idempotencyKey, BookAppointmentRequest request) {
        // Fast path – return if already present
        var existing = appointmentIdempotencyStore.getIfPresent(idempotencyKey);
        if (existing != null) return BookAppointmentResult.builder().appointmentId(existing).newlyCreated(false).build();

        // Get or create a lock per idempotency key
        var lock = appointmentLockMap.get(idempotencyKey, k -> new Object());

        synchronized (lock) {
            try {
                // Recheck after acquiring lock (double-checked locking)
                existing = appointmentIdempotencyStore.getIfPresent(idempotencyKey);
                if (existing != null) return BookAppointmentResult.builder().appointmentId(existing).newlyCreated(false).build();

                var duration = appointmentProperties.getDurationMinutes();
                appointmentValidator.validateAppointment(request, duration);

                var appointmentId = UUID.randomUUID().toString();
                var success = appointmentBookingStrategy.book(request, duration, appointmentId);
                if (!success) {
                    log.info("Slot already booked, not saving appointment for key: {}", request.getOwnerId());
                    throw new SlotAlreadyBookedException(request.getOwnerId()); // Booking failed
                }

                appointmentIdempotencyStore.put(idempotencyKey, appointmentId);
                return BookAppointmentResult.builder().appointmentId(appointmentId).newlyCreated(true).build();
            } finally {
                // clean up lockMap explicitly to avoid memory bloat
                appointmentLockMap.invalidate(idempotencyKey);
            }
        }
    }

    public UpcomingAppointmentsResponseDto getUpcomingAppointments(String ownerId, int page, int size) {
        if (userService.getUser(ownerId).isEmpty()) {
            throw new UserNotFoundException(ownerId);
        }
        var now = LocalDateTime.now(clock);
        var pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        var pagedAppointments = appointmentRepository.findByOwnerIdAndStartTimeAfter(ownerId, now, pageable);

        if (pagedAppointments.isEmpty()) {
            return UpcomingAppointmentsResponseDto.builder()
                    .success(true)
                    .message("No upcoming appointments found")
                    .appointments(List.of())  // empty list
                    .currentPage(page)
                    .totalPages(0)
                    .totalItems(0)
                    .build();
        }
        // N+1 Query Problem
        // Inside the .map(), we are calling userService.getUser(inviteeId) for every appointment.
        // If there are 100 appointments, this will make 100 DB/API calls.

        // Batch fetch invitees
        var inviteeIds = pagedAppointments.stream()
                .map(Appointment::getInviteeId)
                .collect(Collectors.toSet());

        var inviteeMap = userService.getUsersByIds(inviteeIds); // Map<String, User>

        var upcomingAppointments =  pagedAppointments.stream()
                .map(a -> {
                    //fetch invitee details
                    //Any relevant details about the Invitee or the appointment
                    var invitee = inviteeMap.getOrDefault(a.getInviteeId(), null);
                    var inviteeName = invitee != null ? invitee.getName() : null;
                    var inviteeEmail = invitee != null ? invitee.getEmail() : null;

                    return UpcomingAppointmentResponse.builder()
                            .appointmentId(a.getAppointmentId())
                            .startTime(DateUtils.formatDateTime(a.getStartTime()))
                            .endTime(DateUtils.formatDateTime(a.getEndTime()))
                            .inviteeId(a.getInviteeId())
                            .inviteeName(inviteeName)
                            .inviteeEmail(inviteeEmail)
                            .build();
                })
                .toList();

        return UpcomingAppointmentsResponseDto.builder()
                .success(true)
                .message("Fetched upcoming appointments successfully")
                .appointments(upcomingAppointments)
                .currentPage(pagedAppointments.getNumber())
                .totalPages(pagedAppointments.getTotalPages())
                .totalItems(pagedAppointments.getTotalElements())
                .build();
    }
}
