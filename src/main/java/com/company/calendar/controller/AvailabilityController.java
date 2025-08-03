package com.company.calendar.controller;

import com.company.calendar.dto.availability.AvailabilitySetupResponse;
import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import com.company.calendar.dto.availability.AvailableSlotsResponse;
import com.company.calendar.service.availability.AvailabilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/setup")
    public ResponseEntity<AvailabilitySetupResponse> createAvailability(@RequestBody @Valid AvailabilityRuleSetupRequest request) {
        //C in CRUD
        //Use Only if you want a first-time creation endpoint that fails if rules already exist for the user.
        //POST → used once per ownerId; error if already exists.
        availabilityService.createAvailabilityRules(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AvailabilitySetupResponse.builder()
                        .success(true)
                        .message("Availability rules created successfully")
                        .build());
    }

    @PutMapping("/setup")
    public ResponseEntity<AvailabilitySetupResponse> setAvailability(@RequestBody @Valid AvailabilityRuleSetupRequest request) {
        //This is like a create or overwrite all endpoint for setting availability for fist time by owner
        //avoiding PATCH. Can be a future requirement.
        //idempotency key handling can be done in PUT
        //exception handling changes to be done
        availabilityService.updateAvailabilityRules(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AvailabilitySetupResponse.builder()
                        .success(true)
                        .message("Availability rules set successfully")
                        .build());
    }


    @GetMapping("/{ownerId}/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable
            @NotBlank
            String ownerId,
            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must be 0 or greater")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            int size){
        //Search Available Time Slots API: Implement an API endpoint that allows an Invitee to
        //search for available time slots on a particular date.
        //actually in cal.com, its a month but for now we are supporting day.
        var slots = availabilityService.getAvailableSlots(ownerId, date);
        return ResponseEntity.ok(
                AvailableSlotsResponse.builder()
                        .success(true)
                        .message("Available slots fetched successfully.")
                        .slots(slots)
                        .build()
        );
    }
}
