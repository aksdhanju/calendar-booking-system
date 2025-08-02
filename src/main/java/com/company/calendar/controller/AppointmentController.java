package com.company.calendar.controller;

import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.dto.BookAppointmentResponse;
import com.company.calendar.service.appointment.AppointmentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<BookAppointmentResponse> bookAppointment(@RequestBody @Valid BookAppointmentRequest request) {
        var appointmentId = appointmentService.bookAppointment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookAppointmentResponse.builder()
                        .success(true)
                        .message("Appointment booked successfully.")
                        .appointmentId(appointmentId)
                        .build());
    }
}
