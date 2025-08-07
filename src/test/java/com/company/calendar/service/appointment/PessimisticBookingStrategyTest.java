package com.company.calendar.service.appointment;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.entity.Appointment;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
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
    private String ownerId;
    private String inviteeId;
    private LocalDateTime startDateTime;

    @BeforeEach
    void setUp() {
        ownerId = "3";
        inviteeId = "1";
        startDateTime = LocalDateTime.of(2025, 8, 25, 22, 0);
        appointmentId = UUID.randomUUID().toString();
        request = BookAppointmentRequest.builder()
                .inviteeId(ownerId)
                .ownerId(inviteeId)
                .startDateTime(startDateTime)
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

    //You are simulating a concurrent booking scenario:
    //
    //Two threads attempt to book the same slot (ownerId, startTime).
    //
    //The first one should succeed and save the appointment.
    //
    //The second one should fail because the slot is already taken.
    //
    //To simulate this properly in a unit test (with mocks),
    // we must simulate changes in system state over time,
    // even though mocks don't retain actual state.
    // That’s where AtomicInteger and CountDownLatch come in.
    @Test
    void testConcurrentBooking_sameSlot() throws InterruptedException {
        CountDownLatch thread1Ready = new CountDownLatch(1);
        CountDownLatch thread2Proceed = new CountDownLatch(1);
        CountDownLatch allDone = new CountDownLatch(2);

        AtomicInteger counter = new AtomicInteger(0);

        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .ownerId(ownerId)
                .inviteeId(inviteeId)
                .startDateTime(startDateTime)
                .build();

        Lock lock = new ReentrantLock();
        when(appointmentOwnerLockManager.getLock(anyString())).thenReturn(lock);

        // Mock behavior for repository
        // First thread sees no booking
        // Second thread sees that appointment is already booked
        when(appointmentRepository.existsByOwnerIdAndStartTime(ownerId, startDateTime))
                .thenAnswer(invocation -> counter.get() > 0);

        doAnswer(invocation -> {
            // Simulate saving by incrementing counter
            counter.incrementAndGet();
            return null;
        }).when(appointmentRepository).save(any(Appointment.class));

        Runnable thread1 = () -> {
            thread1Ready.countDown(); // Notify thread2 to proceed
            boolean result = pessimisticBookingStrategy.book(request, 60, appointmentId);
            System.out.println("Thread 1 result: " + result);
            assertTrue(result);
            allDone.countDown();
        };

        Runnable thread2 = () -> {
            try {
                thread1Ready.await(); // Wait for thread1 to be ready (about to acquire lock)
                thread2Proceed.countDown(); // Let both run now
                Thread.sleep(100); // Delay to ensure thread1 saves before this runs
                boolean result = pessimisticBookingStrategy.book(request, 60, appointmentId);
                System.out.println("Thread 2 result: " + result);
                assertFalse(result);
            } catch (InterruptedException e) {
                fail("Thread interrupted");
            }
            allDone.countDown();
        };

        new Thread(thread1).start();
        new Thread(thread2).start();

        allDone.await(2, TimeUnit.SECONDS);
        verify(appointmentOwnerLockManager, times(2)).getLock(ownerId);
        verify(appointmentOwnerLockManager, times(2)).releaseLock(ownerId);
    }
}