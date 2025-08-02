package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.dto.appointment.UpcomingAppointmentResponse;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.service.user.UserService;
import com.company.calendar.validator.AppointmentTimeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    private final AppointmentTimeValidator appointmentTimeValidator;
    private final AppointmentProperties appointmentProperties;
    private final AppointmentBookingStrategy appointmentBookingStrategy;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    // Book an appointment if it's not already taken
    public boolean bookAppointment(String appointmentId, BookAppointmentRequest request) {
        if (appointmentRepository.existsById(appointmentId)) {
            return false; // Already booked with same ID, idempotent response
        }
        var startTime = request.getStartTime();
        var duration = appointmentProperties.getDurationMinutes();
        var endTime = startTime.plusMinutes(duration);
        appointmentTimeValidator.validate(startTime, endTime);
        log.info("{}: appointment time validation passed", appointmentId);
        return appointmentBookingStrategy.book(request, duration, appointmentId);
    }

    public UpcomingAppointmentsResponseDto getUpcomingAppointments(String ownerId, int page, int size) {
        var now = LocalDateTime.now();
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
                            .startTime(a.getStartTime())
                            .endTime(a.getEndTime())
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
