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
    @Size(max = 30, message = "Id must be between 1 and 30 characters")
    private String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    @Size(max = 20, message = "Id must be between 1 and 20 characters")
    private String email;
}
