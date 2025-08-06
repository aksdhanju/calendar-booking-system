package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetAvailableSlotsTest {

    @Mock
    private UserService userService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityServiceHelper availabilityServiceHelper;

    @InjectMocks
    private AvailabilityService availabilityService;

    private String ownerId;
    private final LocalDate date = LocalDate.of(2025, 8, 9); // Saturday

    @BeforeEach
    void setUp() {
        ownerId = "1";
    }

    @Test
    @DisplayName("Owner Id does not exist in system")
    void testForNonExistentOwner() {
        doThrow(new UserNotFoundException(ownerId)).when(userService).validateUserExists(any());
        var ex = assertThrows(
                UserNotFoundException.class,
                () -> availabilityService.getAvailableSlots(ownerId, date)
        );

        assertEquals("User not found with id: " + ownerId, ex.getMessage());
    }

    @Test
    @DisplayName("Should return empty list when no availability rules exist for the day")
    void testForEmptyRules() {
        doNothing().when(userService).validateUserExists(any());
        when(availabilityServiceHelper.getRulesForOwnerAndDay(anyString(), any()))
                .thenReturn(List.of());
        var slots = availabilityService.getAvailableSlots(ownerId, date);
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Should return all available slots when no appointments exist")
    void testForSlotsWhenNoAppointments() {
        doNothing().when(userService).validateUserExists(any());

        var rule = AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(date.getDayOfWeek())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        when(availabilityServiceHelper.getRulesForOwnerAndDay(anyString(), any()))
                .thenReturn(List.of(rule));
        when(appointmentRepository.findByOwnerIdAndDate(anyString(), any())).thenReturn(List.of());

        List<AvailableSlotDto> slotsExpected = List.of(
                AvailableSlotDto.builder()
                        .startDateTime(LocalDateTime.of(date, LocalTime.of(9, 0)))
                        .endDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
                        .build(),
                AvailableSlotDto.builder()
                        .startDateTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
                        .endDateTime(LocalDateTime.of(date, LocalTime.of(11, 0)))
                        .build(),
                AvailableSlotDto.builder()
                        .startDateTime(LocalDateTime.of(date, LocalTime.of(11, 0)))
                        .endDateTime(LocalDateTime.of(date, LocalTime.of(12, 0)))
                        .build()
        );

        when(availabilityServiceHelper.generateAvailableSlotsFromRules(any(), any(), any()))
                .thenReturn(slotsExpected);

        var slotsActual = availabilityService.getAvailableSlots(ownerId, date);
        assertEquals(slotsExpected.size(), slotsActual.size());
    }

    @Test
    @DisplayName("Should exclude booked slots")
    void testForSlotsExcludingAppointments() {

    }

    @Test
    @DisplayName("Should return multiple slots for multiple rules")
    void testForSlotsWithMultipleRules() {
    }
}