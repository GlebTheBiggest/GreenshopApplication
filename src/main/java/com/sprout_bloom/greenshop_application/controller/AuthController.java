package com.sprout_bloom.greenshop_application.controller;

import com.sprout_bloom.greenshop_application.dto.UserRegistrationDto;
import com.sprout_bloom.greenshop_application.entity.User;
import com.sprout_bloom.greenshop_application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "success", required = false) boolean success, Model model) {
        if(success) model.addAttribute("registrationSuccess", true);
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                               BindingResult bindingResult,
                               Model model) {
        log.info("Starting registration for: {}", userDto.getUsername());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors detected");
            return "register";
        }

        try {
            if (!userService.isEmailEligibleForRegistration(userDto.getEmail())) {
                model.addAttribute("error", "Email вже використовується або невалідний");
                return "register";
            }

            if (!userService.isPhoneNumberEligibleForRegistration(userDto.getPhone())) {
                model.addAttribute("error", "Номер телефону вже використовується або невалідний");
                return "register";
            }

            if (!userService.isUsernameUniqueAndProper(userDto.getUsername())) {
                model.addAttribute("error", "Логін вже зайнятий або невалідний");
                return "register";
            }

            User registeredUser = userService.registerUser(userDto);
            log.info("User registered: ID={}", registeredUser.getId());
            return "redirect:/login?success=true";

        } catch (DataIntegrityViolationException e) {
            return handleDuplicateError(e, model);
        } catch (RuntimeException e) {
            log.error("Помилка збереження:", e);
            model.addAttribute("error", "Помилка сервера. Спробуйте пізніше.");
            return "register";
        }
    }

    private String handleDuplicateError(DataIntegrityViolationException e, Model model) {
        String errorMessage = "Помилка реєстрації: ";
        String exceptionMessage = e.getMostSpecificCause().getMessage();

        if (exceptionMessage.contains("users_email_key")) {
            errorMessage += "Email вже використовується";
        } else if (exceptionMessage.contains("users_username_key")) {
            errorMessage += "Логін вже зайнятий";
        } else if (exceptionMessage.contains("users_phone_key")) {
            errorMessage += "Номер телефону вже використовується";
        } else {
            errorMessage += "Конфлікт даних. Перевірте введені дані";
        }

        model.addAttribute("error", errorMessage);
        return "register";
    }
}