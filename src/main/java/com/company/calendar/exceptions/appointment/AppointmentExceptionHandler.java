package com.company.calendar.exceptions.appointment;

import com.company.calendar.exceptions.BaseErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(2)
public class AppointmentExceptionHandler {

    @ExceptionHandler(AvailableSlotNotFoundException.class)
    public ResponseEntity<BaseErrorResponse> handleFreeSlotNotFound(AvailableSlotNotFoundException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<BaseErrorResponse> handleSlotAlreadyBookedException(SlotAlreadyBookedException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}