package com.company.calendar.config.swagger.availability;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.company.calendar.dto.availability.AvailableSlotsResponse;
import com.company.calendar.exceptions.BaseErrorResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available slots fetched successfully for owner id: 1",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = AvailableSlotsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid ownerId or date",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = BaseErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Failed to fetch slots due to server error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = BaseErrorResponse.class)))
})
public @interface ApiResponsesGetAvailableSlots {}
