package com.company.calendar.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String id) {
        super("User already exists with id: " + id);
    }
}