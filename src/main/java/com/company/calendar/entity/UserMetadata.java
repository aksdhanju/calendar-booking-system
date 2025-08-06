package com.company.calendar.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMetadata {
    private String name;
    private String email;
}
