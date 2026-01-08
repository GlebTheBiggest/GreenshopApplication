package com.sproutnbloom.greenshopapplication.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Set<OrderDto> orders;
    private CartDto cart;
}