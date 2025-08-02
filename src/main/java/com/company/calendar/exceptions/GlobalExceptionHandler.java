package com.company.calendar.exceptions;

import com.company.calendar.dto.appointment.BookAppointmentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BookAppointmentResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        BookAppointmentResponse response = BookAppointmentResponse.builder()
                .success(false)
                .message("Validation failed: " + errorMessages)
                .errorCode("VALIDATION_ERROR")
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BookAppointmentResponse> handleIllegalArgument(IllegalArgumentException ex) {
        BookAppointmentResponse response = BookAppointmentResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("INVALID_REQUEST")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BookAppointmentResponse> handleSlotUnavailable(IllegalStateException ex) {
        BookAppointmentResponse response = BookAppointmentResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("SLOT_UNAVAILABLE")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BookAppointmentResponse> handleGeneric(Exception ex) {
        BookAppointmentResponse response = BookAppointmentResponse.builder()
                .success(false)
                .message("Unexpected error: " + ex.getMessage())
                .errorCode("INTERNAL_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
