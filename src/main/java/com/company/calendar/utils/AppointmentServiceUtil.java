package com.company.calendar.utils;

import com.company.calendar.dto.appointment.UpcomingAppointmentResponse;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.entity.User;
import com.company.calendar.entity.UserMetadata;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AppointmentServiceUtil {

    private AppointmentServiceUtil() {
    }

    public static UpcomingAppointmentResponse toResponse(Appointment appointment, Map<String, User> inviteeMap) {
        var invitee = inviteeMap.get(appointment.getInviteeId());

        var inviteeName = Optional.ofNullable(invitee)
                .map(User::getUserMetadata)
                .map(UserMetadata::getName)
                .orElse(null);

        var inviteeEmail = Optional.ofNullable(invitee)
                .map(User::getUserMetadata)
                .map(UserMetadata::getEmail)
                .orElse(null);

        return UpcomingAppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .startTime(DateUtils.formatDateTime(appointment.getStartTime()))
                .endTime(DateUtils.formatDateTime(appointment.getEndTime()))
                .inviteeId(appointment.getInviteeId())
                .inviteeName(inviteeName)
                .inviteeEmail(inviteeEmail)
                .build();
    }

    public static UpcomingAppointmentsResponseDto emptyResponse(String ownerId, int page) {
        return UpcomingAppointmentsResponseDto.builder()
                .success(true)
                .message("No upcoming appointments found for owner id: " + ownerId)
                .appointments(List.of())
                .currentPage(page)
                .totalPages(0)
                .totalItems(0)
                .build();
    }

    public static UpcomingAppointmentsResponseDto successResponse(
            String ownerId,
            Page<Appointment> pagedAppointments,
            List<UpcomingAppointmentResponse> upcomingAppointments
    ) {
        return UpcomingAppointmentsResponseDto.builder()
                .success(true)
                .message("Fetched upcoming appointments successfully for owner id: " + ownerId)
                .appointments(upcomingAppointments)
                .currentPage(pagedAppointments.getNumber())
                .totalPages(pagedAppointments.getTotalPages())
                .totalItems(pagedAppointments.getTotalElements())
                .build();
    }
}
