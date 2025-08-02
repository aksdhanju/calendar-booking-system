package com.company.calendar.entity;

import com.company.calendar.enums.UserType;
import lombok.Getter;

@Getter
public class User {
    private String id;
    //metadata
    private String name;
    private String email;
}
