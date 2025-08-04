package com.company.calendar.exceptions.appointment;

public class AvailableSlotNotFoundException extends RuntimeException {
    public AvailableSlotNotFoundException(String startDateTime, String id) {
        super("No available slot found at: " + startDateTime + " for owner: " + id);
    }
}