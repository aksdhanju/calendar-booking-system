package com.company.calendar.service;

import com.company.calendar.config.AppointmentProperties;
import com.company.calendar.repository.appointment.AppointmentRepository;
import com.company.calendar.repository.availabilityRule.AvailabilityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class UpdateAvailabilityRulesTest {

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentProperties appointmentProperties;

    @InjectMocks
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @Description("")
    void testAvailabilityRulesSetSuccess() {

    }
}
