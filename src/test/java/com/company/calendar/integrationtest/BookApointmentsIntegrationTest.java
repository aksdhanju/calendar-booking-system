package com.company.calendar.integrationtest;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResult;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.entity.User;
import com.company.calendar.entity.UserMetadata;
import com.company.calendar.exceptions.appointment.SlotAlreadyBookedException;
import com.company.calendar.repository.user.UserRepository;
import com.company.calendar.service.appointment.AppointmentService;
import com.company.calendar.service.availability.AvailabilityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class BookApointmentsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    AppointmentService appointmentService;

    @Autowired
    AvailabilityService availabilityService;

    @Autowired
    UserRepository userRepository;

    @Test
    @Description("Test concurrent booking by two users")
    public void TestConcurrentBookingByTwoUsers() throws ExecutionException, InterruptedException {
        //create user
        var user1 = User.builder()
                .id("1")
                .userMetadata(UserMetadata.builder()
                        .name("Gallen")
                        .email("gsimmons@dealmerridion.com")
                        .build())
                .build();
        userRepository.save(user1);
        var user2 = User.builder()
                .id("2")
                .userMetadata(UserMetadata.builder()
                        .name("Mohit")
                        .email("maggarwal@dealmerridion.com")
                        .build())
                .build();
        userRepository.save(user2);
        var user3 = User.builder()
                .id("3")
                .userMetadata(UserMetadata.builder()
                        .name("Akash")
                        .email("asingh@gmail.com")
                        .build())
                .build();
        userRepository.save(user3);
        var user4 = User.builder()
                .id("4")
                .userMetadata(UserMetadata.builder()
                        .name("Prince")
                        .email("pkumar@gmail.com")
                        .build())
                .build();
        userRepository.save(user4);

        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rulesList = new ArrayList<>();
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.SATURDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rulesList.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.SUNDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        var availabilityRuleSetupRequest = AvailabilityRuleSetupRequest.builder()
                .ownerId(user1.getId())
                .rules(rulesList)
                .build();

        availabilityService.createAvailabilityRules(availabilityRuleSetupRequest);

        var startTime = LocalDateTime.of(2025, 8, 11, 23, 0, 0);
        var request1 = BookAppointmentRequest.builder()
                .ownerId(user1.getId())
                .inviteeId(user3.getId())
                .startDateTime(startTime)
                .build();

        var request2 = BookAppointmentRequest.builder()
                .ownerId(user1.getId())
                .inviteeId(user4.getId())
                .startDateTime(startTime)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<BookAppointmentResult> future1 = executor.submit(() -> {
            try {
                return appointmentService.bookAppointment(UUID.randomUUID().toString(), request1);
            } catch (SlotAlreadyBookedException e) {
                log.warn("Thread 1: Slot already booked");
                return BookAppointmentResult.builder()
                        .newlyCreated(false)
                        .build();
            }
        });

        Future<BookAppointmentResult> future2 = executor.submit(() -> {
            try {
                return appointmentService.bookAppointment(UUID.randomUUID().toString(), request2);
            } catch (SlotAlreadyBookedException e) {
                log.warn("Thread 2: Slot already booked");
                return BookAppointmentResult.builder()
                        .newlyCreated(false)
                        .build();
            }
        });

        var result1 = future1.get();
        var result2 = future2.get();

        executor.shutdown();

        long successCount = Stream.of(result1, result2)
                .filter(BookAppointmentResult::isNewlyCreated)
                .count();

        assertEquals(1, successCount, "Exactly one booking should succeed");


        var upcomingAppointments = appointmentService.getUpcomingAppointments(user1.getId(), 0, 10);
        if (upcomingAppointments == null || upcomingAppointments.getAppointments() == null) {
            log.info("No upcoming appointments found.");
            return;
        }

        upcomingAppointments.getAppointments().forEach(appointment ->
                log.info("AppointmentId: {}, StartTime: {}, EndTime: {}, InviteeId: {}, InviteeName: {}, InviteeEmail: {}",
                        appointment.getAppointmentId(),
                        appointment.getStartTime(),
                        appointment.getEndTime(),
                        appointment.getInviteeId(),
                        appointment.getInviteeName(),
                        appointment.getInviteeEmail()
                )
        );
    }
}
