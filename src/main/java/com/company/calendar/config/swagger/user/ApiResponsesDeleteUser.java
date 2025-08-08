package com.company.calendar.config.swagger.user;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "User deleted successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
            {
              "success": true,
              "message": "User deleted successfully for id: 1"
            }
        """))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid input",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
            {
              "success": false,
              "message": "<sample bad request exception message>"
            }
        """))
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
            {
              "success": false,
              "message": "Unexpected server error while deleting user"
            }
        """))
        )
})
public @interface ApiResponsesDeleteUser {}