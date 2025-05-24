package com.sprout_bloom.greenshop_application.controller;

import com.sprout_bloom.greenshop_application.dto.UserRegistrationDto;
import com.sprout_bloom.greenshop_application.entity.User;
import com.sprout_bloom.greenshop_application.exception.InvalidRegistrationException;
import com.sprout_bloom.greenshop_application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto, Model model) {
        log.info("Starting registration for: {}", userDto.getUsername());

        try {
            // Перевірка валідності email
            if (!userService.isEmailEligibleForRegistration(userDto.getEmail())) {
                log.warn("Невірний email: {}", userDto.getEmail());
                model.addAttribute("error", "Email is not eligible for registration!");
                return "register";
            }

            // Перевірка валідності номера телефону
            if (!userService.isPhoneNumberEligibleForRegistration(userDto.getPhone())) {
                log.warn("Невірний номер телефону: {}", userDto.getPhone());
                model.addAttribute("error", "Phone number is not valid for registration!");
                return "register";
            }

            // Перевірка унікальності імені користувача
            if (!userService.isUsernameUniqueAndProper(userDto.getUsername())) {
                log.warn("Некоректний або зайнятий username: {}", userDto.getUsername());
                model.addAttribute("error", "Username is not valid or already taken!");
                return "register";
            }

            // Створення та збереження користувача
            User registeredUser = userService.registerUser(userDto);
            log.info("User registered: ID={}", registeredUser.getId());

            return "redirect:/login?success";

        } catch (RuntimeException e) { // Ловимо всі Runtime-помилки
            log.error("Помилка збереження:", e);
            model.addAttribute("error", "Помилка сервера. Спробуйте пізніше.");
            return "register";
        }
    }

    @PostMapping("/login-success")
    public String loginSuccess() {
        log.info("User successfully logged in.");
        return "redirect:/index"; // Перенаправлення на index.html після успішного входу
    }
}
