package com.company.calendar.exceptions.appointment;

public class SlotAlreadyBookedException extends RuntimeException {
    public SlotAlreadyBookedException(String ownerId) {
        super("Appointment slot already booked for owner: " + ownerId);
    }
}
