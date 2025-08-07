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
                responseCode = "200",
                description = "Upcoming appointments fetched successfully"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid pagination or path parameter"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Unexpected server error while fetching appointments"
        )
})
public @interface ApiResponsesGetUpcomingAppointments {}
