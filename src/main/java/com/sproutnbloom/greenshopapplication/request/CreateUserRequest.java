package com.sproutnbloom.greenshopapplication.request;

import com.sproutnbloom.greenshopapplication.enums.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String password;
}