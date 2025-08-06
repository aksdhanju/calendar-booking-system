package com.company.calendar.service.availability;

import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import com.company.calendar.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAvailabilityRulesTest {

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private AvailabilityServiceHelper availabilityServiceHelper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AvailabilityService availabilityService;

    private AvailabilityRuleSetupRequest defaultRequest;
    private String ownerId;
    private AvailabilityRuleSetupRequest.AvailabilityRuleRequest defaultRule;

    @BeforeEach
    void setUp() {
        ownerId = "1";
        defaultRule = AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();
        defaultRequest = AvailabilityRuleSetupRequest.builder()
                .ownerId(ownerId)
                .rules(List.of(defaultRule))
                .build();
    }

    @Test
    @DisplayName("Owner Id does not exist in system")
    void testForNonExistentOwner() {
        doThrow(new UserNotFoundException(ownerId)).when(userService).validateUserExists(any());
        var ex = assertThrows(
                UserNotFoundException.class,
                () -> availabilityService.updateAvailabilityRules(defaultRequest)
        );

        assertEquals("User not found with id: " + ownerId, ex.getMessage());
    }

    @Test
    @DisplayName("Call update availability rules api for first time")
    void testUpdateRulesForFirstTime() {
        doNothing().when(userService).validateUserExists(any());
        when(availabilityServiceHelper.mergeOverlappingSlots(any())).thenReturn(List.of(defaultRule));
        when(availabilityRuleRepository.findByOwnerId(any())).thenReturn(List.of());
        doNothing().when(availabilityRuleRepository).save(any(), any());
        var result = availabilityService.updateAvailabilityRules(defaultRequest);
        assertEquals("Availability rules created successfully for owner id: " + ownerId, result.getMessage());
        assertFalse(result.isCreated());
    }

    @Test
    @DisplayName("Call update availability rules api for second time")
    void testUpdateRulesForSecondTime() {
        doNothing().when(userService).validateUserExists(any());
        when(availabilityServiceHelper.mergeOverlappingSlots(any())).thenReturn(List.of(defaultRule));
        when(availabilityRuleRepository.findByOwnerId(any())).thenReturn(List.of(AvailabilityRule.builder().build()));
        doNothing().when(availabilityRuleRepository).save(any(), any());
        var result = availabilityService.updateAvailabilityRules(defaultRequest);
        assertEquals("Availability rules updated successfully for owner id: " + ownerId, result.getMessage());
        assertTrue(result.isCreated());
    }
}
