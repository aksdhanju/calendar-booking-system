package com.company.calendar.exceptions;

public class InvalidStartDateTimeException extends RuntimeException {
    public InvalidStartDateTimeException(String message) {
        super(message);
    }
}