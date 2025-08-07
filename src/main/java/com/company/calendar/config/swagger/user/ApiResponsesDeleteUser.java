package com.company.calendar.config.swagger.user;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user id"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error while deleting user")
})
public @interface ApiResponsesDeleteUser {}
