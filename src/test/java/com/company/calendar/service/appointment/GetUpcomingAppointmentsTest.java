package com.company.calendar.service.appointment;

import com.company.calendar.dto.user.GetUserResponse;
import com.company.calendar.dto.user.UserResponse;
import com.company.calendar.entity.Appointment;
import com.company.calendar.entity.User;
import com.company.calendar.entity.UserMetadata;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUpcomingAppointmentsTest {
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppointmentService appointmentService;

    private String ownerId;
    private int page;
    private int size;

    @BeforeEach
    void setUp() {
        ownerId = "1";
        page = 0;
        size = 10;
    }

    @Test
    @DisplayName("Owner Id does not exist in system")
    void testForNonExistentOwner() {
        doThrow(new UserNotFoundException(ownerId)).when(userService).validateUserExists(any());
        var ex = assertThrows(
                UserNotFoundException.class,
                () -> appointmentService.getUpcomingAppointments(ownerId, page, size)
        );

        assertEquals("User not found with id: " + ownerId, ex.getMessage());
    }

    @Test
    @DisplayName("Owner Id does not exist in system")
    void testValidUserButEmptyPagedAppointments() {
        doNothing().when(userService).validateUserExists(any());
        when(appointmentRepository.findByOwnerIdAndStartTimeAfter(
                eq(ownerId), any(LocalDateTime.class), any(PageRequest.class))
        ).thenReturn(Page.empty());

        var response = appointmentService.getUpcomingAppointments(ownerId, page, size);

        assertNotNull(response);
        assertTrue(response.getAppointments().isEmpty());
        assertTrue(response.isSuccess());
        assertEquals("No upcoming appointments found for owner id: 1", response.getMessage());
        assertEquals(page, response.getCurrentPage());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalItems());
    }

    @Test
    @DisplayName("Valid user with non-empty upcoming appointments page")
    void testValidUserWithPagedAppointments() {
        page = 0;
        size = 2;
        LocalDateTime now = LocalDateTime.now();
        String inviteeId = "3";
        doNothing().when(userService).validateUserExists(any());

        var appointment1 = Appointment.builder()
                .appointmentId("a1")
                .ownerId(ownerId)
                .inviteeId(inviteeId)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusMinutes(60))
                .build();

        var appointment2 = Appointment.builder()
                .appointmentId("a2")
                .ownerId(ownerId)
                .inviteeId(inviteeId)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(2).plusMinutes(60))
                .build();

        var appointmentPage = new PageImpl<>(
                List.of(appointment1, appointment2),
                PageRequest.of(page, size),
                2
        );

        when(appointmentRepository.findByOwnerIdAndStartTimeAfter(
                eq(ownerId), any(LocalDateTime.class), any(PageRequest.class))
        ).thenReturn(appointmentPage);

        var userMetadata = UserMetadata.builder()
                .name("Akash")
                .email("asingh@gmail.com")
                .build();
        var invitee = User.builder()
                .id(inviteeId)
                .userMetadata(userMetadata)
                .build();
        when(userService.getUsersByIds(Set.of(inviteeId)))
                .thenReturn(Map.of(inviteeId, invitee));

        var result = appointmentService.getUpcomingAppointments(ownerId, page, size);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Fetched upcoming appointments successfully for owner id: " + ownerId, result.getMessage());
        assertEquals(2, result.getAppointments().size());
        assertEquals(page, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalItems());

        var appt = result.getAppointments().getFirst();
        assertEquals("a1", appt.getAppointmentId());
        assertEquals(inviteeId, appt.getInviteeId());
        assertEquals("Akash", appt.getInviteeName());
        assertEquals("asingh@gmail.com", appt.getInviteeEmail());
    }
}