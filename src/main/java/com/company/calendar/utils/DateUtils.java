package com.company.calendar.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.company.calendar.constants.ApplicationConstants.YYYY_MM_DD_HH_MM_SS_FORMAT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_FORMAT);

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}
