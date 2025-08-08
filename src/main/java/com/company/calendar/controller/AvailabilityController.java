package com.company.calendar.controller;

import com.company.calendar.config.swagger.availability.ApiResponsesCreateAvailability;
import com.company.calendar.config.swagger.availability.ApiResponsesGetAvailableSlots;
import com.company.calendar.config.swagger.availability.ApiResponsesSetAvailability;
import com.company.calendar.dto.availability.AvailabilityRuleSetupResponse;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.dto.availability.AvailableSlotsResponse;
import com.company.calendar.service.availability.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Availability", description = "APIs for managing and viewing availability rules and slots")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Operation(summary = "Create availability rules", description = "Creates availability rules for the owner. Fails if already exists.")
    @ApiResponsesCreateAvailability
    @PostMapping("/setup")
    public ResponseEntity<AvailabilityRuleSetupResponse> createAvailability(@RequestBody @Valid AvailabilityRuleSetupRequest request) {
        log.info("Received POST /setup request to create availability rules for ownerId: {}", request.getOwnerId());
        var message = availabilityService.createAvailabilityRules(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AvailabilityRuleSetupResponse.builder()
                        .success(true)
                        .message(message)
                        .build());
    }

    @Operation(summary = "Update availability rules", description = "Sets or overwrites availability rules for the owner.")
    @ApiResponsesSetAvailability
    @PutMapping("/setup")
    public ResponseEntity<AvailabilityRuleSetupResponse> setAvailability(@RequestBody @Valid AvailabilityRuleSetupRequest request) {
        log.info("Received PUT /setup request to set/update availability rules for ownerId: {}", request.getOwnerId());
        var result = availabilityService.updateAvailabilityRules(request);
        return ResponseEntity
                .status(result.isCreated() ? HttpStatus.OK : HttpStatus.CREATED)
                .body(AvailabilityRuleSetupResponse.builder()
                        .success(true)
                        .message(result.getMessage())
                        .build());
    }

    @Operation(summary = "Get available slots", description = "Fetches available time slots for the given owner on a specific date.")
    @GetMapping("/{ownerId}/slots")
    @ApiResponsesGetAvailableSlots
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable
            @NotBlank
            @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Owner Id can only contain letters, digits, hyphens, and underscores")
            @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
            String ownerId,
            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @FutureOrPresent
            LocalDate date) {
        log.info("Received GET /{}/slots request for ownerId: {} on date: {}", ownerId, ownerId, date);
        var slots = availabilityService.getAvailableSlots(ownerId, date);
        var message = slots.isEmpty() ? "No Available slots found" : "Available slots fetched successfully";
        log.info("Slot fetch result for ownerId: {} on {}: {}", ownerId, date, message);
        return ResponseEntity.ok(
                AvailableSlotsResponse.builder()
                        .success(true)
                        .message(message + " for owner id: " + ownerId)
                        .slots(slots)
                        .build()
        );
    }
}
