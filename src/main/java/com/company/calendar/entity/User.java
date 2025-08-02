package com.company.calendar.entity;

import com.company.calendar.enums.UserType;
import lombok.Getter;

@Getter
public class User {
    private String id;
    private UserType userType;
    //metadata
    private String name;
    private String emailId;
}
