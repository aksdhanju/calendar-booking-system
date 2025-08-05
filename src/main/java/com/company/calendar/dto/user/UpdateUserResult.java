package com.company.calendar.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserResult {
    private String message;
    private boolean created;
}
