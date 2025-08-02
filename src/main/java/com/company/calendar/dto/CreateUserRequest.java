package com.company.calendar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateUserRequest {
    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @Email
    private String email;
}
