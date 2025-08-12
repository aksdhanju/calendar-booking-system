package com.company.calendar.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@NotNull
@Builder
@Getter
@Schema(description = "Request to update an existing user")
public final class UpdateUserRequest {
    @NotBlank(message = "Name should not be blank")
    @Size(max = 50, message = "Id must be between 1 and 50 characters")
    @Schema(description = "Name of the user", example = "Akashdeep Singh")
    private final String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    @Size(max = 50, message = "Id must be between 1 and 50 characters")
    @Schema(description = "Email address of the user", example = "akash.singh@gmail.com")
    private final String email;
}
