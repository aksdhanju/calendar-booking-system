package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResult;
import com.company.calendar.exceptions.InvalidStartDateTimeException;
import com.company.calendar.exceptions.appointment.SlotAlreadyBookedException;
import com.company.calendar.service.user.UserService;
import com.company.calendar.validator.AppointmentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookAppointmentTest {

    @Mock
    private AppointmentProperties appointmentProperties;

    @Mock
    private AppointmentBookingStrategy appointmentBookingStrategy;

    @Mock
    private AppointmentIdempotencyStore appointmentIdempotencyStore;

    @Mock
    private AppointmentLockManager appointmentLockManager;

    @Mock
    private AppointmentValidator appointmentValidator;

    @InjectMocks
    private AppointmentService appointmentService;

    private final static String idempotencyKey = "unique-key-123";

    private BookAppointmentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = BookAppointmentRequest.builder()
                .ownerId("1")
                .inviteeId("3")
                .startDateTime(LocalDateTime.of(2025, 8, 25, 22, 0)) // aligned with hour
                .build();
        when(appointmentProperties.getDurationMinutes()).thenReturn(60);
    }

    @Test
    @DisplayName("Pass idempotency key already present in system")
    void testDuplicateBooking() {
        var appointmentId = UUID.randomUUID().toString();
        when(appointmentIdempotencyStore.get(any())).thenReturn(appointmentId);
        BookAppointmentResult result = appointmentService.bookAppointment(idempotencyKey, validRequest);

        assertNotNull(result);
        assertFalse(result.isNewlyCreated());
        assertEquals("Appointment already exists for owner id: 1", result.getMessage());
        assertEquals(appointmentId, result.getAppointmentId());
    }

    @Test
    @DisplayName("Pass invalid appointment time in request")
    void testInvalidAppointmentTime() {
        when(appointmentIdempotencyStore.get(any())).thenReturn(null);
        Lock object = new ReentrantLock();
        when(appointmentLockManager.getLock(any())).thenReturn(object);
        var invalidRequest = BookAppointmentRequest.builder()
                .ownerId("1")
                .inviteeId("3")
                .startDateTime(LocalDateTime.of(2025, 8, 25, 22, 30))
                .build();
        when(appointmentValidator.validateAppointment(any(), anyLong()))
                .thenThrow(new InvalidStartDateTimeException("Appointments must start at the top of the hour and last 60 minutes"));
        doNothing().when(appointmentLockManager).releaseLock(anyString());

        var ex = assertThrows(
                InvalidStartDateTimeException.class,
                () -> appointmentService.bookAppointment(idempotencyKey, invalidRequest)
        );

        assertEquals("Appointments must start at the top of the hour and last 60 minutes", ex.getMessage());
        verify(appointmentLockManager).releaseLock(idempotencyKey);
    }

    @Test
    @DisplayName("Appointment time is valid, key is unique but slot is not free so booking failed")
    void testBookingFailed() {
        when(appointmentIdempotencyStore.get(any())).thenReturn(null);
        Lock object = new ReentrantLock();
        when(appointmentLockManager.getLock(any())).thenReturn(object);
        when(appointmentValidator.validateAppointment(any(), anyLong())).thenReturn(true);
        when(appointmentBookingStrategy.book(any(), anyInt(), anyString())).thenReturn(false);
        doNothing().when(appointmentLockManager).releaseLock(anyString());

        var ex = assertThrows(
                SlotAlreadyBookedException.class,
                () -> appointmentService.bookAppointment(idempotencyKey, validRequest)
        );

        assertEquals("Appointment slot already booked for owner: 1", ex.getMessage());

        verify(appointmentLockManager).releaseLock(idempotencyKey);
    }

    @Test
    @DisplayName("Should book appointment successfully if slot is free, appointment time is valid, key is unique")
    void testBookAppointment() {
        when(appointmentIdempotencyStore.get(any())).thenReturn(null);
        Lock object = new ReentrantLock();
        when(appointmentLockManager.getLock(any())).thenReturn(object);
        when(appointmentValidator.validateAppointment(any(), anyLong())).thenReturn(true);
        when(appointmentBookingStrategy.book(any(), anyInt(), anyString())).thenReturn(true);
        doNothing().when(appointmentLockManager).releaseLock(anyString());
        doNothing().when(appointmentIdempotencyStore).put(anyString(), anyString());

        BookAppointmentResult result = appointmentService.bookAppointment(idempotencyKey, validRequest);

        assertNotNull(result);
        assertTrue(result.isNewlyCreated());
        assertEquals("Appointment booked successfully for owner id: 1", result.getMessage());
        assertNotNull(result.getAppointmentId());

        verify(appointmentValidator).validateAppointment(validRequest, 60);
        verify(appointmentBookingStrategy).book(validRequest, 60, result.getAppointmentId());
        verify(appointmentLockManager).releaseLock(idempotencyKey);
        verify(appointmentIdempotencyStore).put(idempotencyKey, result.getAppointmentId());
    }
}
