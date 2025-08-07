package com.company.calendar.exceptions.availability;

import com.company.calendar.controller.AvailabilityController;
import com.company.calendar.dto.availability.AvailabilityRuleSetupResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(3)
@RestControllerAdvice
@Slf4j
public class AvailabilityExceptionHandler {

    @ExceptionHandler(AvailabilityRulesAlreadyExistsException.class)
    public ResponseEntity<AvailabilityRuleSetupResponse> handleRulesAlreadyExists(AvailabilityRulesAlreadyExistsException ex) {
        AvailabilityRuleSetupResponse response = AvailabilityRuleSetupResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
