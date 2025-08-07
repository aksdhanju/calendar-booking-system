package com.company.calendar.config.swagger.user;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "201", description = "User created because it did not exist"),
        @ApiResponse(responseCode = "400", description = "Invalid input for user update"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error while updating user")
})
public @interface ApiResponsesUpdateUser {}
