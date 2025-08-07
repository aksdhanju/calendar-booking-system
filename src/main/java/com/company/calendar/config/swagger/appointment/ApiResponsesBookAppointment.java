package com.company.calendar.config.swagger.appointment;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Appointment booked successfully"
        ),
        @ApiResponse(
                responseCode = "200",
                description = "Appointment already exists for the given idempotency key"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request payload"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Unexpected server error while booking appointment"
        )
})
public @interface ApiResponsesBookAppointment {}
