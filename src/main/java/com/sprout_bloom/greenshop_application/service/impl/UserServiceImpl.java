package com.sprout_bloom.greenshop_application.service.impl;

import com.sprout_bloom.greenshop_application.dto.UserRegistrationDto;
import com.sprout_bloom.greenshop_application.entity.User;
import com.sprout_bloom.greenshop_application.exception.InvalidRegistrationException;
import com.sprout_bloom.greenshop_application.repository.UserRepository;
import com.sprout_bloom.greenshop_application.service.UserService;
import com.sprout_bloom.greenshop_application.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.sprout_bloom.greenshop_application.enums.Role.USER;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistrationDto userDto) {
        log.info("Starting user registration: {}", userDto.getUsername());

        if (!isEmailEligibleForRegistration(userDto.getEmail())) {
            log.warn("Invalid email: {}", userDto.getEmail());
            throw new InvalidRegistrationException("Email is not eligible for registration.");
        }
        if (!isPhoneNumberEligibleForRegistration(userDto.getPhone())) {
            log.warn("Invalid phone number: {}", userDto.getPhone());
            throw new InvalidRegistrationException("Phone number is not eligible for registration.");
        }
        if (!isUsernameUniqueAndProper(userDto.getUsername())) {
            log.warn("Invalid or already taken username: {}", userDto.getUsername());
            throw new InvalidRegistrationException("Username is not valid or already taken.");
        }
        if (!PasswordValidator.isValid(userDto.getPassword())) {
            log.warn("Password does not meet security requirements.");
            throw new InvalidRegistrationException("Password does not meet the security requirements.");
        }

        // Створення нового користувача
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(USER);
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone().replaceAll("[\\s-]+", " "));
        user.setAddress(userDto.getAddress());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: ID={}, Username={}", savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    @Override
    public boolean isEmailEligibleForRegistration(String email) {
        boolean isUnique = !userRepository.existsByEmail(email);
        boolean isValid = email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        log.debug("[UserService] Email check - Unique: {}, Valid: {}", isUnique, isValid);
        return isUnique && isValid;
    }

    @Override
    public boolean isPhoneNumberEligibleForRegistration(String phoneNumber) {
        // Нормалізація номера перед перевіркою
        String normalizedPhone = phoneNumber.replaceAll("[\\s-]+", " ");
        boolean isUnique = !userRepository.existsByPhone(normalizedPhone);
        boolean isValid = normalizedPhone.matches("^\\+48 \\d{3} \\d{3} \\d{3}$");
        return isUnique && isValid;
    }

    @Override
    public boolean isUsernameUniqueAndProper(String username) {
        return !userRepository.existsByUsername(username) && username.matches("^[a-zA-Z0-9._-]{5,20}$");
    }
}