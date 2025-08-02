package com.company.calendar.dto.user;

import lombok.Builder;

@Builder
public class UserResponse<T>  {
    private boolean success;
    private String message;
    private T data;
}

