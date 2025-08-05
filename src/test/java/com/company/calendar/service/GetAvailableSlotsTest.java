//package com.company.calendar.service;
//
//import com.company.calendar.config.AppointmentProperties;
//import com.company.calendar.dto.availability.AvailableSlotDto;
//import com.company.calendar.dto.user.GetUserResponse;
//import com.company.calendar.dto.user.UserResponse;
//import com.company.calendar.entity.Appointment;
//import com.company.calendar.entity.AvailabilityRule;
//import com.company.calendar.exceptions.user.UserNotFoundException;
//import com.company.calendar.repository.appointment.AppointmentRepository;
//import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
//import com.company.calendar.service.availability.AvailabilityService;
//import com.company.calendar.service.user.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.*;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class GetAvailableSlotsTest {
//
//    @Mock
//    private AvailabilityRuleRepository availabilityRuleRepository;
//
//    @Mock
//    private AppointmentRepository appointmentRepository;
//
//    @Mock
//    private AppointmentProperties appointmentProperties;
//
//    @Mock
//    private UserService userService;
//
//    @InjectMocks
//    private AvailabilityService availabilityService;
//
//    private final String ownerId = "owner-123";
//    private final LocalDate date = LocalDate.of(2025, 8, 2); // Saturday
//
//    @BeforeEach
//    void setUp() {
//        reset(availabilityRuleRepository, appointmentRepository, appointmentProperties, userService);
//    }
//
//    private Optional<UserResponse<GetUserResponse>> mockUserResponse(String id) {
//        var userDto = GetUserResponse.builder()
//                .id(id)
//                .name("Test User")
//                .email("test@example.com")
//                .build();
//
//        var response = UserResponse.<GetUserResponse>builder()
//                .success(true)
//                .message("Fetched")
//                .data(userDto)
//                .build();
//
//        return Optional.of(response);
//    }
//
//    @Test
//    @DisplayName("Should throw UserNotFoundException if user does not exist")
//    void shouldThrowWhenUserNotFound() {
//        when(userService.getUser(ownerId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> availabilityService.getAvailableSlots(ownerId, date))
//                .isInstanceOf(UserNotFoundException.class)
//                .hasMessageContaining(ownerId);
//    }
//
//    @Test
//    @DisplayName("Should return empty list when no availability rules exist for the day")
//    void shouldReturnEmptyWhenNoRules() {
//        when(userService.getUser(ownerId)).thenReturn(mockUserResponse(ownerId));
//        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeek(ownerId, date.getDayOfWeek()))
//                .thenReturn(List.of());
//
//        List<AvailableSlotDto> slots = availabilityService.getAvailableSlots(ownerId, date);
//
//        assertThat(slots).isEmpty();
//    }
//
//    @Test
//    @DisplayName("Should return all available slots when no appointments exist")
//    void shouldReturnSlotsWhenNoAppointments() {
//        when(userService.getUser(ownerId)).thenReturn(mockUserResponse(ownerId));
//
//        var rule = AvailabilityRule.builder()
//                .ownerId(ownerId)
//                .dayOfWeek(date.getDayOfWeek())
//                .startTime(LocalTime.of(9, 0))
//                .endTime(LocalTime.of(14, 0))
//                .build();
//
//        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeek(ownerId, date.getDayOfWeek()))
//                .thenReturn(List.of(rule));
//        when(appointmentRepository.findByOwnerIdAndDate(ownerId, date)).thenReturn(List.of());
//        when(appointmentProperties.getDurationMinutes()).thenReturn(60);
//
//        List<AvailableSlotDto> slots = availabilityService.getAvailableSlots(ownerId, date);
//
//        assertThat(slots).hasSize(5);
//        assertThat(slots).allMatch(AvailableSlotDto::isBookable);
//    }
//
//    @Test
//    @DisplayName("Should exclude booked slots")
//    void shouldExcludeBookedSlots() {
//        when(userService.getUser(ownerId)).thenReturn(mockUserResponse(ownerId));
//
//        var rule = AvailabilityRule.builder()
//                .ownerId(ownerId)
//                .dayOfWeek(date.getDayOfWeek())
//                .startTime(LocalTime.of(10, 0))
//                .endTime(LocalTime.of(11, 0))
//                .build();
//
//        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeek(ownerId, date.getDayOfWeek()))
//                .thenReturn(List.of(rule));
//
//        var appointment = Appointment.builder()
//                .startTime(LocalDateTime.of(date, LocalTime.of(10, 0)))
//                .build();
//
//        when(appointmentRepository.findByOwnerIdAndDate(ownerId, date)).thenReturn(List.of(appointment));
//        when(appointmentProperties.getDurationMinutes()).thenReturn(30);
//
//        List<AvailableSlotDto> slots = availabilityService.getAvailableSlots(ownerId, date);
//
//        assertThat(slots).hasSize(1);
//        assertThat(slots.get(0).getStartDateTime().toLocalTime()).isEqualTo(LocalTime.of(10, 30));
//    }
//
//    @Test
//    @DisplayName("Should return multiple slots for multiple rules")
//    void shouldReturnSlotsForMultipleRules() {
//        when(userService.getUser(ownerId)).thenReturn(mockUserResponse(ownerId));
//
//        var rule1 = AvailabilityRule.builder()
//                .ownerId(ownerId)
//                .dayOfWeek(date.getDayOfWeek())
//                .startTime(LocalTime.of(9, 0))
//                .endTime(LocalTime.of(10, 0))
//                .build();
//
//        var rule2 = AvailabilityRule.builder()
//                .ownerId(ownerId)
//                .dayOfWeek(date.getDayOfWeek())
//                .startTime(LocalTime.of(11, 0))
//                .endTime(LocalTime.of(12, 0))
//                .build();
//
//        when(availabilityRuleRepository.findByOwnerIdAndDayOfWeek(ownerId, date.getDayOfWeek()))
//                .thenReturn(List.of(rule1, rule2));
//        when(appointmentRepository.findByOwnerIdAndDate(ownerId, date)).thenReturn(List.of());
//        when(appointmentProperties.getDurationMinutes()).thenReturn(30);
//
//        List<AvailableSlotDto> slots = availabilityService.getAvailableSlots(ownerId, date);
//
//        assertThat(slots).hasSize(4); // 2 slots per rule
//        assertThat(slots).extracting(s -> s.getStartDateTime().toLocalTime())
//                .containsExactlyInAnyOrder(
//                        LocalTime.of(9, 0), LocalTime.of(9, 30),
//                        LocalTime.of(11, 0), LocalTime.of(11, 30)
//                );
//    }
//}
