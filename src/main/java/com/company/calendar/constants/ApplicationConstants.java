package com.company.calendar.constants;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ApplicationConstants {
    public static final String YYYY_MM_DD_HH_MM_SS_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ALPHANUMERIC_HYPHEN_UNDERSCORE_REGEX = "^[a-zA-Z0-9_-]+$";
}