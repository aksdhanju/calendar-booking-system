package com.company.calendar.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@NotNull
public class UpdateUserRequest {
    @NotBlank(message = "Name should not be blank")
    private String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    private String email;
}
