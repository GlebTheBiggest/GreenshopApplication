package com.sproutnbloom.greenshopapplication.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
}