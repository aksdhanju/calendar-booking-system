package com.company.calendar.exceptions.availability;

public class AvailabilityRulesAlreadyExistsException  extends RuntimeException {
    public AvailabilityRulesAlreadyExistsException(String id) {
        super("Availability rules already exist for owner: " + id);
    }
}