package com.company.calendar.config.swagger.user;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user request or user already exists"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error while creating user")
})
public @interface ApiResponsesCreateUser {}
