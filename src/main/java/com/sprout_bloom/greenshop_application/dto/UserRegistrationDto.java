package com.sprout_bloom.greenshop_application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 10, message = "First name cannot be longer than 10 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name cannot be longer than 20 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+48\\s\\d{3}\\s\\d{3}\\s\\d{3}$", message = "Phone must be in format +48 XXX XXX XXX")
    private String phone;

    @Size(max = 254, message = "Address cannot be longer than 254 characters")
    private String address;
}