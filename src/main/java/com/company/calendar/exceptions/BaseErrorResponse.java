package com.company.calendar.exceptions;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BaseErrorResponse {
    private boolean success;
    private String message;
}
