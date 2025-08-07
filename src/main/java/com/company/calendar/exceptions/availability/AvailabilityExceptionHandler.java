package com.company.calendar.exceptions.availability;

import com.company.calendar.controller.AvailabilityController;
import com.company.calendar.dto.availability.AvailabilityRuleSetupResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(3)
@RestControllerAdvice(basePackageClasses = AvailabilityController.class)
public class AvailabilityExceptionHandler {

    @ExceptionHandler(AvailabilityRulesAlreadyExistsException.class)
    public ResponseEntity<AvailabilityRuleSetupResponse> handleRulesAlreadyExists(AvailabilityRulesAlreadyExistsException ex) {
        AvailabilityRuleSetupResponse response = AvailabilityRuleSetupResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
