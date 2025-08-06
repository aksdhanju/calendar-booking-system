package com.company.calendar.service.appointment;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResult;
import com.company.calendar.service.user.UserService;
import com.company.calendar.validator.AppointmentValidator;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private Cache<String, String> appointmentIdempotencyStore;

    @Mock
    private Cache<String, Object> appointmentLockMap;

    @Mock
    private AppointmentValidator appointmentValidator;

    @Mock
    private UserService userService;

    @Mock
    private Clock clock;

    @InjectMocks
    private AppointmentService appointmentService;

    private final static String IDEMPOTENCY_KEY = "unique-key-123";

    private BookAppointmentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = BookAppointmentRequest.builder()
                .ownerId("1")
                .inviteeId("3")
                .startDateTime(LocalDateTime.of(2025, 8, 25, 22, 0)) // aligned with hour
                .build();

        when(appointmentProperties.getDurationMinutes()).thenReturn(60);
        when(clock.instant()).thenReturn(Instant.parse("2025-08-25T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Should book appointment successfully if slot is free and key is unique")
    void testBookAppointment_success() {
        // Setup mocks
        // You can define stubbings here
        lenient().when(appointmentProperties.getDurationMinutes()).thenReturn(30);

        // Mocking the behavior of appointmentLockMap to return a lock object
//        lenient().when(appointmentLockMap.get(eq(IDEMPOTENCY_KEY), any()))
//                .thenAnswer(invocation -> new Object());

        when(appointmentIdempotencyStore.getIfPresent(IDEMPOTENCY_KEY)).thenReturn(null);
        when(appointmentLockMap.get(any(), any())).thenAnswer(invocation -> new Object());
        when(appointmentBookingStrategy.book(any(),anyInt(), any())).thenReturn(true);
        when(appointmentValidator.validateAppointment(any(), anyLong())).thenReturn(true);

        // Act
        BookAppointmentResult result = appointmentService.bookAppointment(IDEMPOTENCY_KEY, validRequest);

        // Verify
        assertNotNull(result);
        assertTrue(result.isNewlyCreated());
        assertEquals("Appointment booked successfully for owner id: 1", result.getMessage());
        assertNotNull(result.getAppointmentId());

//        verify(appointmentValidator).validateAppointment(validRequest, 60);
//        verify(appointmentBookingStrategy).book(validRequest, 60, result.getAppointmentId());
//        verify(appointmentLockMap).invalidate(IDEMPOTENCY_KEY);
    }
}
