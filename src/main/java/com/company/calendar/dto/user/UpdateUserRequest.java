package com.company.calendar.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@NotNull
@Builder
@Getter
public class UpdateUserRequest {
    @NotBlank(message = "Name should not be blank")
    @Size(max = 50, message = "Id must be between 1 and 50 characters")
    private String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    @Size(max = 50, message = "Id must be between 1 and 50 characters")
    private String email;
}
