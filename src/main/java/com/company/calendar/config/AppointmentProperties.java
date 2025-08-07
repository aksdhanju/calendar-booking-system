package com.company.calendar.config;

import com.company.calendar.enums.AppointmentBookingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "appointment")
@Getter
@Setter
public class AppointmentProperties {
    private int durationMinutes = 60;
    private String timeValidatorStrategy = "fullHour";
    private String bookingStrategy = AppointmentBookingStrategy.OPTIMISTIC.name();
    private String repository = "in-memory";
}
