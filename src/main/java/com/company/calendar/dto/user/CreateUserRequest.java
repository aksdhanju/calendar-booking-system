package com.company.calendar.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateUserRequest {
    @NotBlank(message = "Id should not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Id can only contain letters, digits, hyphens, and underscores")
    @Size(max = 64, message = "Id must be between 1 and 64 characters")
    private String id;

    @NotBlank(message = "Name should not be blank")
    @Size(max = 30, message = "Id must be between 1 and 30 characters")
    private String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    @Size(max = 20, message = "Id must be between 1 and 20 characters")
    private String email;
}
