package com.company.calendar.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.company.calendar.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Invalid request: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseErrorResponse> handleIllegalState(IllegalStateException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Slot unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseErrorResponse> handleDeserializationError(HttpMessageNotReadableException ex) {
        //This exception is thrown during deserialization (before validation).
        Throwable cause = ex.getCause();
        String message = "Invalid request format.";

        if (cause instanceof InvalidFormatException ife) {
            // Optional: Check if it's about LocalTime
            if (ife.getTargetType().equals(LocalTime.class)) {
                message = "Invalid time format. Please use a valid HH:mm between 00:00 and 23:00";
            } else {
                message = "Invalid value: " + ife.getValue();
            }
        }

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message(message)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseErrorResponse> handleHttpMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Method not allowed: " + ex.getMethod())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        String globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        String errorMessages = fieldErrors + (fieldErrors.isEmpty() || globalErrors.isEmpty() ? "" : "; ") + globalErrors;

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Validation failed: " + errorMessages)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handleGeneric(Exception ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Unexpected error: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
