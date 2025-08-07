package com.company.calendar.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "Standard response wrapper for user APIs")
public class UserResponse<T>  {
    @Schema(description = "Indicates success or failure", example = "true")
    private boolean success;
    @Schema(description = "Response message", example = "User created successfully")
    private String message;
    @Schema(description = "Optional data payload")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}

