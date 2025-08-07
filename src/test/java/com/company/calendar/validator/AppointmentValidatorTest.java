package com.company.calendar.validator;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.exceptions.InvalidStartDateTimeException;
import com.company.calendar.exceptions.appointment.AvailableSlotNotFoundException;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.service.availability.AvailabilityServiceHelper;
import com.company.calendar.service.user.UserService;
import com.company.calendar.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AppointmentValidatorTest {

    @Mock
    private UserService userService;

    @Mock
    private AvailabilityServiceHelper availabilityServiceHelper;

    @Mock
    private AppointmentTimeValidator appointmentTimeValidator;

    @InjectMocks
    private AppointmentValidator appointmentValidator;

    private String ownerId;
    private String inviteeId;
    private LocalDateTime startDateTime;

    private BookAppointmentRequest request;


    @BeforeEach
    void setUp() {
        ownerId = "1";
        inviteeId = "3";
        startDateTime = LocalDateTime.of(2025, 8, 25, 22, 0);
        request = BookAppointmentRequest.builder()
                .ownerId(ownerId)
                .inviteeId(inviteeId)
                .startDateTime(startDateTime)
                .build();
    }

    @Test
    @DisplayName("Owner Id does not exist in system")
    void testForNonExistentOwner() {
        doThrow(new UserNotFoundException(ownerId)).when(userService).validateUserExists(any());
        var ex = assertThrows(
                UserNotFoundException.class,
                () -> appointmentValidator.validateAppointment(request, 60)
        );

        assertEquals("User not found with id: " + ownerId, ex.getMessage());
    }

    @Test
    @DisplayName("Test when startDateTime has minutes as 30")
    void testForInvalidStartTime() {
        doNothing().when(userService).validateUserExists(any());
        var invalidRequest = BookAppointmentRequest.builder()
                .ownerId(ownerId)
                .inviteeId(inviteeId)
                .startDateTime(LocalDateTime.of(2025, 8, 25, 22, 30))
                .build();
        doThrow(new InvalidStartDateTimeException("Appointments must start at the top of the hour and last 60 minutes"))
                .when(appointmentTimeValidator).validate(any(), any());

        var ex = assertThrows(
                InvalidStartDateTimeException.class,
                () -> appointmentValidator.validateAppointment(invalidRequest, 60)
        );
        assertEquals("Appointments must start at the top of the hour and last 60 minutes", ex.getMessage());
    }

    @Test
    @DisplayName("Test when no free slots are present")
    void testWhenNoFreeSlotsPresent() {
        doNothing().when(userService).validateUserExists(any());
        doNothing().when(appointmentTimeValidator).validate(any(), any());
        when(availabilityServiceHelper.getRulesForOwnerAndDay(any(), any()))
                .thenReturn(List.of());
        var ex = assertThrows(
                AvailableSlotNotFoundException.class,
                () -> appointmentValidator.validateAppointment(request, 60)
        );
        assertEquals("No available slot found at: " + DateUtils.formatDateTime(startDateTime) + " for owner: " + ownerId, ex.getMessage());
    }

    @Test
    @DisplayName("Test when free slots are present but our booking time is not one among them")
    void testWhenFreeSlotsPresent() {
        doNothing().when(userService).validateUserExists(any());
        doNothing().when(appointmentTimeValidator).validate(any(), any());
        List<AvailabilityRule> availabilityRules = new ArrayList<>();
        availabilityRules.add(AvailabilityRule.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(23, 0))
                .ownerId(ownerId)
                .build());
        when(availabilityServiceHelper.getRulesForOwnerAndDay(any(), any()))
                .thenReturn(availabilityRules);
        List<AvailableSlotDto> availableSlotDtos = new ArrayList<>();
        availableSlotDtos.add(AvailableSlotDto.builder()
                .startDateTime(DateUtils.formatDateTime(startDateTime))
                .endDateTime(DateUtils.formatDateTime(startDateTime.plusMinutes(60)))
                .build());
        when(availabilityServiceHelper.generateAvailableSlotsFromRules(any(), any(), any()))
                .thenReturn(availableSlotDtos);

        when(availabilityServiceHelper.generateAvailableSlotsFromRules(any(), any(), any()))
                .thenReturn(List.of());
        var ex = assertThrows(
                AvailableSlotNotFoundException.class,
                () -> appointmentValidator.validateAppointment(request, 60)
        );
        assertEquals("No available slot found at: " + DateUtils.formatDateTime(startDateTime) + " for owner: " + ownerId, ex.getMessage());
    }

    @Test
    @DisplayName("Test when free slots are present and our booking time is one among them")
    void testCorrectAppointmentValidation() {
        doNothing().when(userService).validateUserExists(any());
        doNothing().when(appointmentTimeValidator).validate(any(), any());
        List<AvailabilityRule> availabilityRules = new ArrayList<>();
        availabilityRules.add(AvailabilityRule.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(23, 0))
                .ownerId(ownerId)
                .build());
        when(availabilityServiceHelper.getRulesForOwnerAndDay(any(), any()))
                .thenReturn(availabilityRules);
        List<AvailableSlotDto> availableSlotDtos = new ArrayList<>();
        availableSlotDtos.add(AvailableSlotDto.builder()
                .startDateTime(DateUtils.formatDateTime(startDateTime))
                .endDateTime(DateUtils.formatDateTime(startDateTime.plusMinutes(60)))
                .build());
        when(availabilityServiceHelper.generateAvailableSlotsFromRules(any(), any(), any()))
                .thenReturn(availableSlotDtos);

        var result = appointmentValidator.validateAppointment(request, 60);
        assertTrue(result);
    }
}