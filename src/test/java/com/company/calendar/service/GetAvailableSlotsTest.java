package com.company.calendar.service;

import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.entity.Appointment;
import com.company.calendar.entity.AvailabilityRule;
import com.company.calendar.enums.RuleType;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import com.company.calendar.service.availability.AvailabilityService;
import com.company.calendar.service.availability.AvailabilityServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class GetAvailableSlotsTest {

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityServiceHelper availabilityServiceHelper;

    @InjectMocks
    private AvailabilityService availabilityService;

    private final String ownerId = "owner-123";
    private final LocalDate date = LocalDate.of(2025, 8, 2); // Saturday

    @BeforeEach
    void setUp() {
        reset(availabilityRuleRepository, appointmentRepository, availabilityServiceHelper);
    }

    @Test
    @Description("Should return empty list when no availability rules exist")
    void testNoAvailabilityRules() {
        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeekAndRuleType(
                eq(ownerId), eq(date.getDayOfWeek()), eq(RuleType.AVAILABLE)))
                .thenReturn(List.of());

        List<AvailableSlotDto> result = availabilityService.getAvailableSlots(ownerId, date);
        assertThat(result).isEmpty();

        verify(appointmentRepository, never()).findByOwnerIdAndDate(anyString(), any());
        verify(availabilityServiceHelper, never()).generateAvailableSlotsFromRules(any(), any(), any());
    }

    @Test
    @Description("Should return available slots when no appointments are booked")
    void testWithRulesNoAppointmentsBooked() {
        var rules = List.of(
                AvailabilityRule.builder()
                        .ownerId(ownerId)
                        .dayOfWeek(DayOfWeek.SATURDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(12, 0))
                        .ruleType(RuleType.AVAILABLE)
                        .build()
        );

        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeekAndRuleType(ownerId, DayOfWeek.SATURDAY, RuleType.AVAILABLE))
                .thenReturn(rules);
        when(appointmentRepository.findByOwnerIdAndDate(ownerId, date))
                .thenReturn(List.of());

        List<AvailableSlotDto> mockedSlots = List.of(
                AvailableSlotDto.builder()
                        .startTime(date.atTime(9, 0))
                        .endTime(date.atTime(10, 0))
                        .bookable(true)
                        .build(),
                AvailableSlotDto.builder()
                        .startTime(date.atTime(10, 0))
                        .endTime(date.atTime(11, 0))
                        .bookable(true)
                        .build()
        );

        when(availabilityServiceHelper.generateAvailableSlotsFromRules(eq(rules), eq(Set.of()), eq(date)))
                .thenReturn(mockedSlots);

        List<AvailableSlotDto> result = availabilityService.getAvailableSlots(ownerId, date);
        assertThat(result).hasSize(2).isEqualTo(mockedSlots);

        verify(availabilityServiceHelper).generateAvailableSlotsFromRules(eq(rules), eq(Set.of()), eq(date));
    }

    @Test
    @Description("Should return filtered available slots excluding booked ones")
    void testWithRulesSomeAppointmentsBooked() {
        var rules = List.of(
                AvailabilityRule.builder()
                        .ownerId(ownerId)
                        .dayOfWeek(DayOfWeek.SATURDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(12, 0))
                        .ruleType(RuleType.AVAILABLE)
                        .build()
        );

        var bookedTime = LocalDateTime.of(date, LocalTime.of(10, 0));

        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeekAndRuleType(ownerId, DayOfWeek.SATURDAY, RuleType.AVAILABLE))
                .thenReturn(rules);
        when(appointmentRepository.findByOwnerIdAndDate(ownerId, date))
                .thenReturn(List.of(
                        Appointment.builder().startTime(bookedTime).build()
                ));

        Set<LocalTime> bookedStartTimes = Set.of(LocalTime.of(10, 0));
        List<AvailableSlotDto> availableSlots = List.of(
                AvailableSlotDto.builder()
                        .startTime(date.atTime(9, 0))
                        .endTime(date.atTime(10, 0))
                        .bookable(true)
                        .build(),
                AvailableSlotDto.builder()
                        .startTime(date.atTime(11, 0))
                        .endTime(date.atTime(12, 0))
                        .bookable(true)
                        .build()
        );

        when(availabilityServiceHelper.generateAvailableSlotsFromRules(eq(rules), eq(bookedStartTimes), eq(date)))
                .thenReturn(availableSlots);

        List<AvailableSlotDto> result = availabilityService.getAvailableSlots(ownerId, date);
        assertThat(result).hasSize(2).isEqualTo(availableSlots);
    }

    @Test
    @Description("Should return empty list when all slots are booked")
    void testWithRulesAllAppointmentsBooked() {
        var rules = List.of(
                AvailabilityRule.builder()
                        .ownerId(ownerId)
                        .dayOfWeek(DayOfWeek.SATURDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(10, 0))
                        .ruleType(RuleType.AVAILABLE)
                        .build()
        );

        var appointment = Appointment.builder().startTime(date.atTime(9, 0)).build();

        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeekAndRuleType(ownerId, DayOfWeek.SATURDAY, RuleType.AVAILABLE))
                .thenReturn(rules);
        when(appointmentRepository.findByOwnerIdAndDate(ownerId, date))
                .thenReturn(List.of(appointment));

        Set<LocalTime> bookedStartTimes = Set.of(LocalTime.of(9, 0));

        when(availabilityServiceHelper.generateAvailableSlotsFromRules(eq(rules), eq(bookedStartTimes), eq(date)))
                .thenReturn(List.of());

        List<AvailableSlotDto> result = availabilityService.getAvailableSlots(ownerId, date);
        assertThat(result).isEmpty();
    }
}
