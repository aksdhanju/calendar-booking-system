package com.company.calendar.service.appointment;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.repository.appointment.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PessimisticBookingStrategyTest {
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentOwnerLockManager appointmentOwnerLockManager;

    @InjectMocks
    private PessimisticBookingStrategy pessimisticBookingStrategy;

    private String appointmentId;
    private BookAppointmentRequest request;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID().toString();
        request = BookAppointmentRequest.builder()
                .inviteeId("3")
                .ownerId("1")
                .startDateTime(LocalDateTime.of(2025, 8, 25, 22, 0))
                .build();
    }

    @Test
    @DisplayName("Test for single owner booking already exists")
    void testForSingleOwnerBookingAlreadyExists() {
        when(appointmentOwnerLockManager.getLock(anyString())).thenReturn(new ReentrantLock());
        when(appointmentRepository.existsByOwnerIdAndStartTime(any(), any())).thenReturn(true);
        boolean isBooked = pessimisticBookingStrategy.book(request, 60, appointmentId);
        assertFalse(isBooked);
        verify(appointmentOwnerLockManager).releaseLock(anyString());
    }

    @Test
    @DisplayName("Test for single owner new booking")
    void testForSingleOwnerNewBooking() {
        when(appointmentOwnerLockManager.getLock(anyString())).thenReturn(new ReentrantLock());
        when(appointmentRepository.existsByOwnerIdAndStartTime(any(), any())).thenReturn(false);
        doNothing().when(appointmentRepository).save(any());
        boolean isBooked = pessimisticBookingStrategy.book(request, 60, appointmentId);
        assertTrue(isBooked);
        verify(appointmentOwnerLockManager).releaseLock(anyString());
    }
}