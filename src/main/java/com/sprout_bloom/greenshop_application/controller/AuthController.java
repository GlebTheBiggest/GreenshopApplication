package com.sprout_bloom.greenshop_application.controller;

import com.sprout_bloom.greenshop_application.dto.UserRegistrationDto;
import com.sprout_bloom.greenshop_application.exception.InvalidRegistrationException;
import com.sprout_bloom.greenshop_application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto, Model model) {
        try {
            if (!userService.isEmailEligibleForRegistration(userDto.getEmail())) {
                model.addAttribute("error", "Email is not eligible for registration!");
                return "register";
            }
            if (!userService.isPhoneNumberEligibleForRegistration(userDto.getPhone())) {
                model.addAttribute("error", "Phone number is not valid for registration!");
                return "register";
            }
            if (!userService.isUsernameUniqueAndProper(userDto.getUsername())) {
                model.addAttribute("error", "Username is not valid or already taken!");
                return "register";
            }

            userService.registerUser(userDto);
            return "redirect:/login?success";
        } catch (InvalidRegistrationException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}