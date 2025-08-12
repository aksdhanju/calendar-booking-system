package com.company.calendar.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class UpdateUserResult {
    private final String message;
    private final boolean created;
}
