package com.company.calendar.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Request to create a new user")
public class CreateUserRequest {
    @NotBlank(message = "Id should not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Id must be between 1 and 64 characters")
    @Schema(description = "Unique Id for the user", example = "1")
    private String id;

    @NotBlank(message = "Name should not be blank")
    @Size(max = 50, message = "Name must be between 1 and 50 characters")
    @Schema(description = "Name of the user", example = "Akashdeep Singh")
    private String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    @Size(max = 50, message = "Email must be between 1 and 50 characters")
    @Schema(description = "Email address of the user", example = "akash.singh@gmail.com")
    private String email;
}
