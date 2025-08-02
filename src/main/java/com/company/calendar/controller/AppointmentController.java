package com.company.calendar.controller;

import com.company.calendar.dto.BookAppointmentRequest;
import com.company.calendar.dto.BookAppointmentResponse;
import com.company.calendar.dto.appointment.UpcomingAppointmentsResponseDto;
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
        var created = appointmentService.bookAppointment(appointmentId, request);
        return ResponseEntity
                .status(created ? HttpStatus.CREATED : HttpStatus.OK)
                .body(BookAppointmentResponse.builder()
                        .success(true)
                        .message(created ? "Appointment booked successfully." : "Appointment already exists.")
                        .appointmentId(appointmentId)
                        .build());
    }

    @GetMapping("/owner/{ownerId}/upcoming")
    public ResponseEntity<UpcomingAppointmentsResponseDto> getUpcomingAppointments(
            @PathVariable @NotBlank String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var upcomingAppointments = appointmentService.getUpcomingAppointments(ownerId, page, size);
        return ResponseEntity.ok(upcomingAppointments);
    }
}
