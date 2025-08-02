package com.company.calendar.controller;

import com.company.calendar.dto.availability.AvailabilitySetupResponse;
import com.company.calendar.dto.availability.AvailableSlotDto;
import com.company.calendar.dto.availability.AvailabilitySetupRequest;
import com.company.calendar.service.AvailabilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/setup")
    public ResponseEntity<AvailabilitySetupResponse> createAvailability(@RequestBody @Valid AvailabilitySetupRequest request) {
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
    public ResponseEntity<AvailabilitySetupResponse> setAvailability(@RequestBody @Valid AvailabilitySetupRequest request) {
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


    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotDto>> getAvailableSlots(
            @RequestParam @NotBlank String ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        //Search Available Time Slots API: Implement an API endpoint that allows an Invitee to
        //search for available time slots on a particular date.
        //actually in cal.com, its a month but for now we are supporting day.
        List<AvailableSlotDto> slots = availabilityService.getAvailableSlots(ownerId, date);
        return ResponseEntity.ok(slots);
    }
}
