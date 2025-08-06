//package com.company.calendar.service;
//
//import com.company.calendar.config.AppointmentProperties;
//import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
//import com.company.calendar.entity.AvailabilityRule;
//import com.company.calendar.repository.appointment.AppointmentRepository;
//import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
//import com.company.calendar.service.availability.AvailabilityService;
//import com.company.calendar.service.user.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.time.DayOfWeek;
//import java.time.LocalTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for {@link AvailabilityService#updateAvailabilityRules(AvailabilityRuleSetupRequest)}.
// */
//@ExtendWith(SpringExtension.class)
//class UpdateAvailabilityRulesTest {
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
//    private AvailabilityRuleSetupRequest validRequest;
//
//    @BeforeEach
//    void setUp() {
//        // Prepare a valid single-rule request
//        AvailabilityRuleSetupRequest.AvailabilityRuleRequest rule = AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
//                .dayOfWeek(DayOfWeek.MONDAY)
//                .startTime(LocalTime.of(10, 0))
//                .endTime(LocalTime.of(12, 0))
//                .build();
//
//        validRequest = AvailabilityRuleSetupRequest.builder()
//                .ownerId("owner123")
//                .rules(List.of(rule))
//                .build();
//    }
//
//    /**
//     * @description Test saving a single valid availability rule.
//     */
//    @Test
//    @DisplayName("Should save single valid availability rule")
//    void testUpdateAvailabilityRules_success() {
//        availabilityService.updateAvailabilityRules(validRequest);
//
//        ArgumentCaptor<List<AvailabilityRule>> captor = ArgumentCaptor.forClass(List.class);
//        verify(availabilityRuleRepository).save(eq("owner123"), captor.capture());
//
//        List<AvailabilityRule> savedRules = captor.getValue();
//        assertEquals(1, savedRules.size());
//
//        AvailabilityRule rule = savedRules.get(0);
//        assertEquals(DayOfWeek.MONDAY, rule.getDayOfWeek());
//        assertEquals(LocalTime.of(10, 0), rule.getStartTime());
//        assertEquals(LocalTime.of(12, 0), rule.getEndTime());
//        assertEquals(RuleType.AVAILABLE, rule.getRuleType());
//        assertEquals("owner123", rule.getOwnerId());
//    }
//
//    /**
//     * @description Test saving multiple valid availability rules.
//     */
//    @Test
//    @DisplayName("Should save multiple valid availability rules")
//    void testUpdateAvailabilityRules_multipleRules_success() {
//        AvailabilityRuleSetupRequest.AvailabilityRuleRequest rule1 = AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
//                .dayOfWeek(DayOfWeek.MONDAY)
//                .startTime(LocalTime.of(10, 0))
//                .endTime(LocalTime.of(12, 0))
//                .ruleType(RuleType.AVAILABLE)
//                .build();
//
//        AvailabilityRuleSetupRequest.AvailabilityRuleRequest rule2 = AvailabilityRuleSetupRequest.AvailabilityRuleRequest.builder()
//                .dayOfWeek(DayOfWeek.TUESDAY)
//                .startTime(LocalTime.of(14, 0))
//                .endTime(LocalTime.of(16, 0))
//                .ruleType(RuleType.AVAILABLE)
//                .build();
//
//        AvailabilityRuleSetupRequest request = AvailabilityRuleSetupRequest.builder()
//                .ownerId("owner123")
//                .rules(List.of(rule1, rule2))
//                .build();
//
//        availabilityService.updateAvailabilityRules(request);
//
//        ArgumentCaptor<List<AvailabilityRule>> captor = ArgumentCaptor.forClass(List.class);
//        verify(availabilityRuleRepository).save(eq("owner123"), captor.capture());
//        assertEquals(2, captor.getValue().size());
//    }
//
//    /**
//     * @description Test that an empty rule list still triggers a save with an empty list.
//     */
//    @Test
//    @DisplayName("Should save empty rule list if provided")
//    void testUpdateAvailabilityRules_emptyRulesList_shouldNotSave() {
//        AvailabilityRuleSetupRequest request = AvailabilityRuleSetupRequest.builder()
//                .ownerId("owner123")
//                .rules(List.of()) // Empty rules
//                .build();
//
//        availabilityService.updateAvailabilityRules(request);
//
//        verify(availabilityRuleRepository).save(eq("owner123"), eq(List.of()));
//    }
//
//    /**
//     * @description Test that a null rule list throws NullPointerException and does not save.
//     */
//    @Test
//    @DisplayName("Should throw exception when rules list is null")
//    void testUpdateAvailabilityRules_nullRulesList_shouldThrowException() {
//        AvailabilityRuleSetupRequest request = AvailabilityRuleSetupRequest.builder()
//                .ownerId("owner123")
//                .rules(null)
//                .build();
//
//        assertThrows(NullPointerException.class, () ->
//                availabilityService.updateAvailabilityRules(request)
//        );
//
//        verify(availabilityRuleRepository, never()).save(anyString(), anyList());
//    }
//
////    /**
////     * @description Test that a null ownerId throws NullPointerException and does not save.
////     */
////    @Test
////    @DisplayName("Should throw exception when ownerId is null")
////    void testUpdateAvailabilityRules_nullOwnerId_shouldThrowException() {
////        AvailabilityRuleSetupRequest request = AvailabilityRuleSetupRequest.builder()
////                .ownerId(null)
////                .rules(validRequest.getRules())
////                .build();
////
////        assertThrows(NullPointerException.class, () ->
////                availabilityService.updateAvailabilityRules(request)
////        );
////
////        verify(availabilityRuleRepository, never()).save(anyString(), anyList());
////    }
//
//    /**
//     * @description Test that saving overwrites existing rules (idempotent behavior).
//     */
//    @Test
//    @DisplayName("Should overwrite existing rules with new ones")
//    void testUpdateAvailabilityRules_existingRules_shouldOverwrite() {
//        availabilityService.updateAvailabilityRules(validRequest);
//
//        verify(availabilityRuleRepository).save(eq("owner123"), anyList());
//    }
//}
