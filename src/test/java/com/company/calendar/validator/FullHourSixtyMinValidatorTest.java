package com.company.calendar.validator;

import com.company.calendar.exceptions.InvalidStartDateTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class FullHourSixtyMinValidatorTest {

    @InjectMocks
    private FullHourSixtyMinValidator fullHourSixtyMinValidator;

    @Test
    @DisplayName("Start time has minutes as 45 mins")
    void testStartTimeHasMinutesAs45() {
        LocalDateTime startDateTime = LocalDateTime.of(2025, 8, 25, 22, 45);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 8, 25, 23, 0);
        var ex = assertThrows(
                InvalidStartDateTimeException.class,
                () -> fullHourSixtyMinValidator.validate(startDateTime, endDateTime)
        );

        assertEquals("Appointments must start at the top of the hour and last 60 minutes", ex.getMessage());
    }

    @Test
    @DisplayName("End time has minutes as 30 minutes")
    void testEndTimeHasMinutesAs30() {
        LocalDateTime startDateTime = LocalDateTime.of(2025, 8, 25, 22, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 8, 25, 23, 30);
        var ex = assertThrows(
                InvalidStartDateTimeException.class,
                () -> fullHourSixtyMinValidator.validate(startDateTime, endDateTime)
        );

        assertEquals("Appointment must be exactly 60 minutes long", ex.getMessage());
    }
}