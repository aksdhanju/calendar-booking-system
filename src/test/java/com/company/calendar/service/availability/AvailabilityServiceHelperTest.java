package com.company.calendar.service.availability;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceHelperTest {

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private AppointmentProperties appointmentProperties;

    @InjectMocks
    private AvailabilityServiceHelper availabilityServiceHelper;

    private String ownerId;

    @BeforeEach
    void setUp() {
        ownerId = "1";
    }

    @Test
    @DisplayName("Test generate slots with single slot for multiple days")
    void testForSingleSlotOnMultipleDays() {
        //edge cases related to MIDNIGHT and start of day are covered here also
        when(appointmentProperties.getDurationMinutes()).thenReturn(60);
        List<AvailabilityRule> rules = new ArrayList<>();
        rules.add(AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        rules.add(AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
        rules.add(AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build());

        //some of these booked start times are invalid for particular dayOfWeek
        //those would just be ignored by our logic
        Set<LocalTime> bookedStartTimes = new HashSet<>();
        bookedStartTimes.add(LocalTime.of(0, 0));
        bookedStartTimes.add(LocalTime.of(9, 0));
        bookedStartTimes.add(LocalTime.of(15, 0));
        bookedStartTimes.add(LocalTime.of(22, 0));

        LocalDate date1 = LocalDate.of(2025, 8, 24);
        LocalDate date2 = LocalDate.of(2025, 8, 25);
        LocalDate date3 = LocalDate.of(2025, 8, 26);
        LocalDate date4 = LocalDate.of(2025, 8, 27);
        List<AvailableSlotDto> actualSlots1 = availabilityServiceHelper
                .generateAvailableSlotsFromRules(rules, bookedStartTimes, date1); //SUNDAY
        List<AvailableSlotDto> actualSlots2 = availabilityServiceHelper
                .generateAvailableSlotsFromRules(rules, bookedStartTimes, date2); //MONDAY
        List<AvailableSlotDto> actualSlots3 = availabilityServiceHelper
                .generateAvailableSlotsFromRules(rules, bookedStartTimes, date3); //TUESDAY
        List<AvailableSlotDto> actualSlots4 = availabilityServiceHelper
                .generateAvailableSlotsFromRules(rules, bookedStartTimes, date4); //WEDNESDAY
        assertEquals(0, actualSlots1.size());
        assertEquals(20, actualSlots2.size());
        assertEquals(7, actualSlots3.size());
        assertEquals(8, actualSlots4.size());
        //        actualSlots2.forEach(x -> System.out.println(x.getStartDateTime() + "###" + x.getEndDateTime()));
    }

    @Test
    @DisplayName("Test generate slots with multiple slots on a single day but non overlapping")
    void testForMultipleSlotsOnSingleDay() {
        //Note: there would be no overlapping intervals for a particular day
        // we ensured that while saving rules.
        when(appointmentProperties.getDurationMinutes()).thenReturn(60);
        List<AvailabilityRule> rules = new ArrayList<>();
        rules.add(AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(12, 0))
                .build());
        rules.add(AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(20, 0))
                .build());

        //some of these booked start times are invalid for particular dayOfWeek
        //those would just be ignored by our logic
        Set<LocalTime> bookedStartTimes = new HashSet<>();
        bookedStartTimes.add(LocalTime.of(8, 0));
        bookedStartTimes.add(LocalTime.of(15, 0));
        bookedStartTimes.add(LocalTime.of(19, 0));

        LocalDate date = LocalDate.of(2025, 8, 25); //MONDAY
        List<AvailableSlotDto> actualSlots = availabilityServiceHelper
                .generateAvailableSlotsFromRules(rules, bookedStartTimes, date); //SUNDAY
        assertEquals(9, actualSlots.size());
        //        actualSlots.forEach(x -> System.out.println(x.getStartDateTime() + "###" + x.getEndDateTime()));
    }


    @Test
    @DisplayName("Test generate slots with multiple slots on a single day but non overlapping")
    void testGetRulesForOwnerAndDay() {
        List<AvailabilityRule> rules = new ArrayList<>();
        rules.add(AvailabilityRule.builder()
                .ownerId(ownerId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeek(anyString(), any())).thenReturn(rules);
        var actualRules = availabilityServiceHelper.getRulesForOwnerAndDay(ownerId, DayOfWeek.MONDAY);
        assertEquals(rules.size(), actualRules.size());
    }

    @Test
    @DisplayName("Test when multiple slots on a single day are overlapping Scenario 1")
    void testMergeOverlappingSlotsSingleDayScenario1() {
        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules  = new ArrayList<>();
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(21, 0))
                .build());
        var mergedRules = availabilityServiceHelper.mergeOverlappingSlots(rules);
        assertEquals(LocalTime.of(9, 0), mergedRules.getFirst().getStartTime());
        assertEquals(LocalTime.of(21, 0), mergedRules.getFirst().getEndTime());
        assertEquals(DayOfWeek.MONDAY, mergedRules.getFirst().getDayOfWeek());
    }

    @Test
    @DisplayName("Test when multiple slots on a single day are overlapping Scenario 2")
    void testMergeOverlappingSlotsSingleDayScenario2() {
        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules  = new ArrayList<>();
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(23, 0))
                .build());
        var mergedRules = availabilityServiceHelper.mergeOverlappingSlots(rules);
        assertEquals(LocalTime.of(0, 0), mergedRules.getFirst().getStartTime());
        assertEquals(LocalTime.of(23, 0), mergedRules.getFirst().getEndTime());
        assertEquals(DayOfWeek.MONDAY, mergedRules.getFirst().getDayOfWeek());
    }

    @Test
    @DisplayName("Test when multiple slots on a single day are non overlapping")
    void testMergeNonOverlappingSlotsSingleDay() {
        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules  = new ArrayList<>();
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(12, 0))
                .build());
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(20, 0))
                .build());
        var mergedRules = availabilityServiceHelper.mergeOverlappingSlots(rules);
        assertEquals(2, mergedRules.size());
        assertEquals(LocalTime.of(7, 0), mergedRules.getFirst().getStartTime());
        assertEquals(LocalTime.of(12, 0), mergedRules.getFirst().getEndTime());
        assertEquals(DayOfWeek.MONDAY, mergedRules.getFirst().getDayOfWeek());
        assertEquals(LocalTime.of(15, 0), mergedRules.getLast().getStartTime());
        assertEquals(LocalTime.of(20, 0), mergedRules.getLast().getEndTime());
        assertEquals(DayOfWeek.MONDAY, mergedRules.getLast().getDayOfWeek());
    }

    @Test
    @DisplayName("Test when multiple slots on multiple days are overlapping in respective days")
    void testMergeOverlappingSlotsMultipleDays() {
        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules  = new ArrayList<>();

        // MONDAY
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(4, 0))
                .endTime(LocalTime.of(17, 0))
                .build());
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(20, 0))
                .build());

        // THURSDAY
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(4, 0))
                .endTime(LocalTime.of(10, 0))
                .build());
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build());
        rules.add(AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(21, 0))
                .build());

        // Run merge
        var mergedRules = availabilityServiceHelper.mergeOverlappingSlots(rules);

        assertEquals(3, mergedRules.size());

        // Check merged slot for MONDAY
        var mondayRule = mergedRules.stream()
                .filter(r -> r.getDayOfWeek() == DayOfWeek.MONDAY)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected Monday rule not found"));

        assertEquals(LocalTime.of(4, 0), mondayRule.getStartTime());
        assertEquals(LocalTime.of(20, 0), mondayRule.getEndTime());

        // Check merged slots for THURSDAY
        var thursdayRules = mergedRules.stream()
                .filter(r -> r.getDayOfWeek() == DayOfWeek.THURSDAY)
                .toList();

        assertEquals(2, thursdayRules.size());

        // First THURSDAY slot: 04:00 - 13:00
        var thursdayRule1 = thursdayRules.stream()
                .filter(r -> r.getStartTime().equals(LocalTime.of(4, 0)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected Thursday rule [4:00 - 13:00] not found"));

        assertEquals(LocalTime.of(13, 0), thursdayRule1.getEndTime());

        // Second THURSDAY slot: 16:00 - 21:00
        var thursdayRule2 = thursdayRules.stream()
                .filter(r -> r.getStartTime().equals(LocalTime.of(16, 0)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected Thursday rule [16:00 - 21:00] not found"));

        assertEquals(LocalTime.of(21, 0), thursdayRule2.getEndTime());
    }
}