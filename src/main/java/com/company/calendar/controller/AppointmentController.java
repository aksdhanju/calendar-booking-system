package com.company.calendar.controller;

import com.company.calendar.dto.appointment.BookAppointmentRequest;
import com.company.calendar.dto.appointment.BookAppointmentResponseDto;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
import com.company.calendar.service.appointment.AppointmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Validated
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<BookAppointmentResponseDto> bookAppointment(
                @RequestHeader("Idempotency-Key")
                @NotBlank
                @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Idempotency-Key can only contain letters, digits, hyphens, and underscores")
                @Size(max = 64, message = "Idempotency-Key must be between 1 and 64 characters")
                String idempotencyKey,
                @RequestBody
                @Valid
                @NotNull
                BookAppointmentRequest request) {
        var bookAppointmentResult = appointmentService.bookAppointment(idempotencyKey, request);
        return ResponseEntity
                .status(bookAppointmentResult.isNewlyCreated() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(BookAppointmentResponseDto.builder()
                        .success(true)
                        .message(bookAppointmentResult.getMessage())
                        .appointmentId(bookAppointmentResult.getAppointmentId())
                        .build());
    }

    @GetMapping("/owner/{ownerId}/upcoming")
    public ResponseEntity<UpcomingAppointmentsResponseDto> getUpcomingAppointments(
            @PathVariable
            @NotBlank
            @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Owner Id can only contain letters, digits, hyphens, and underscores")
            @Size(max = 64, message = "Owner Id must be between 1 and 64 characters")
            String ownerId,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must be 0 or greater")
            @Max(value = 100, message = "Page index must not exceed 100")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            int size) {
        var upcomingAppointments = appointmentService.getUpcomingAppointments(ownerId, page, size);
        return ResponseEntity.ok(upcomingAppointments);
    }
}
