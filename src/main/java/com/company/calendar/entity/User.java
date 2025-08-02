package com.company.calendar.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private String id;
    //metadata
    private String name;
    private String email;
}
