package com.company.calendar.service.appointment;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.repository.appointment.InMemoryAppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OptimisticBookingStrategyTest {
    //no mocking is done here
    private OptimisticBookingStrategy optimisticBookingStrategy;

    private String ownerId;
    private String inviteeId;
    private LocalDateTime startDateTime;

    @BeforeEach
    void setUp() {
        ownerId = "3";
        inviteeId = "1";
        startDateTime = LocalDateTime.of(2025, 8, 25, 22, 0);
        var appointmentRepository = new InMemoryAppointmentRepository();
        optimisticBookingStrategy = new OptimisticBookingStrategy(appointmentRepository);
    }

    @Test
    void testConcurrentBooking() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Boolean> bookingTask = () -> {
            readyLatch.countDown(); // Notify ready
            startLatch.await();     // Wait for both threads
            var appointmentId = UUID.randomUUID().toString();
            var request = BookAppointmentRequest.builder()
                    .inviteeId(inviteeId)
                    .ownerId(ownerId)
                    .startDateTime(startDateTime)
                    .build();
            return optimisticBookingStrategy.book(request, 60, appointmentId);
        };

        Future<Boolean> result1 = executor.submit(bookingTask);
        Future<Boolean> result2 = executor.submit(bookingTask);

        readyLatch.await();   // Both threads are ready
        startLatch.countDown(); // Let them start at the same time

        boolean r1 = result1.get();
        boolean r2 = result2.get();

        executor.shutdown();

        // Only one of them should succeed
        assertTrue(r1 ^ r2, "Only one booking should succeed");
    }
}
