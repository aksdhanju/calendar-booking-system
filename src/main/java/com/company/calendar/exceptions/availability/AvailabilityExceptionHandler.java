package com.company.calendar.exceptions.availability;

import com.company.calendar.dto.availability.AvailabilitySetupResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.company.calendar.controller.AvailabilityController.class")
public class AvailabilityExceptionHandler {

    @ExceptionHandler(AvailabilityRulesAlreadyExistsException.class)
    public ResponseEntity<AvailabilitySetupResponse> handleRulesAlreadyExists(AvailabilityRulesAlreadyExistsException ex) {
        AvailabilitySetupResponse response = AvailabilitySetupResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AvailabilitySetupResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        AvailabilitySetupResponse response = AvailabilitySetupResponse.builder()
                .success(false)
                .message("Validation failed: " + errorMessages)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AvailabilitySetupResponse> handleGeneric(Exception ex) {
        AvailabilitySetupResponse response = AvailabilitySetupResponse.builder()
                .success(false)
                .message("Unexpected error: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
