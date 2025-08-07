package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.exceptions.availability.AvailabilityRulesAlreadyExistsException;
import com.company.calendar.repository.availabilityRule.InMemoryAvailabilityRuleRepository;
import com.company.calendar.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateAvailabilityRulesConcurrencyTest {

    @Mock
    private AvailabilityServiceHelper availabilityServiceHelper;

    @Mock
    private UserService userService;

    private AvailabilityService availabilityService;

    private AvailabilityRuleSetupRequest request;

    private AvailabilityRuleSetupRequest.AvailabilityRuleRequest rule;

    @BeforeEach
    void setUp() {
        var availabilityRuleRepository = new InMemoryAvailabilityRuleRepository();

        availabilityService = new AvailabilityService(
                availabilityRuleRepository,
                null,
                availabilityServiceHelper,
                userService
        );

        rule = AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        final String ownerId = "1";
        request = AvailabilityRuleSetupRequest.builder()
                .ownerId(ownerId)
                .rules(List.of(rule))
                .build();
    }

    @Test
    void testConcurrentCreateAvailabilityRules() throws InterruptedException {
        doNothing().when(userService).validateUserExists(any());
        when(availabilityServiceHelper.mergeOverlappingSlots(any()))
                .thenReturn(List.of(rule));

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                String message = availabilityService.createAvailabilityRules(request);
                results.add(message);
            } catch (AvailabilityRulesAlreadyExistsException e) {
                results.add(e.getMessage());
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // Only one should succeed, other should throw exception
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(msg -> msg.contains("created successfully")));
        assertTrue(results.stream().anyMatch(msg -> msg.contains("already exist")));
    }
}
