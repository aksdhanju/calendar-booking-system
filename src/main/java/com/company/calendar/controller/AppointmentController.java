package com.company.calendar.controller;

import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.dto.BookAppointmentResponse;
import com.company.calendar.service.appointment.AppointmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<BookAppointmentResponse> bookAppointment(
            @PathVariable @NotBlank String appointmentId, @RequestBody @Valid BookAppointmentRequest request) {
        boolean created = appointmentService.bookAppointment(appointmentId, request);
        return ResponseEntity
                .status(created ? HttpStatus.CREATED : HttpStatus.OK)
                .body(BookAppointmentResponse.builder()
                        .success(true)
                        .message(created ? "Appointment booked successfully." : "Appointment already exists.")
                        .appointmentId(appointmentId)
                        .build());
    }
}
