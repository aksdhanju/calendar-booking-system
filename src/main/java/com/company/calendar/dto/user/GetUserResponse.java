package com.company.calendar.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserResponse {
    private String id;
    private String name;
    private String email;
}
