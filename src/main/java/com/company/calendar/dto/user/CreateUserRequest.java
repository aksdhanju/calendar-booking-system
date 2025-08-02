package com.company.calendar.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Id should not be blank")
    private String id;

    @NotBlank(message = "Name should not be blank")
    private String name;

    @Email
    @NotBlank(message = "Email should not be blank")
    private String email;
}
