package com.company.calendar.config.swagger;

import com.company.calendar.dto.availability.AvailabilityRuleSetupResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Availability rules created successfully for owner id: 1",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AvailabilityRuleSetupResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid input or rules already exist",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
            {
              "success": false,
              "message": "Availability rules already exist for owner id: abc123"
            }
        """))
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
            {
              "success": false,
              "message": "Unexpected error occurred while creating availability rules"
            }
        """))
        )
})
public @interface ApiResponsesCreateAvailability {}
