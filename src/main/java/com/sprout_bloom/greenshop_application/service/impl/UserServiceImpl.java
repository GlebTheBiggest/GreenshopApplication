package com.sprout_bloom.greenshop_application.service.impl;

import com.sprout_bloom.greenshop_application.dto.UserRegistrationDto;
import com.sprout_bloom.greenshop_application.entity.User;
import com.sprout_bloom.greenshop_application.exception.InvalidRegistrationException;
import com.sprout_bloom.greenshop_application.repository.UserRepository;
import com.sprout_bloom.greenshop_application.service.UserService;
import com.sprout_bloom.greenshop_application.util.Hasher;
import com.sprout_bloom.greenshop_application.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User registerUser(UserRegistrationDto userDto) {
        if (!isEmailEligibleForRegistration(userDto.getEmail())) {
            throw new InvalidRegistrationException("Email is not eligible for registration.");
        }
        if (!isPhoneNumberEligibleForRegistration(userDto.getPhone())) {
            throw new InvalidRegistrationException("Phone number is not eligible for registration.");
        }
        if (!isUsernameUniqueAndProper(userDto.getUsername())) {
            throw new InvalidRegistrationException("Username is not valid or already taken.");
        }
        if (!PasswordValidator.isValid(userDto.getPassword())) {
            throw new InvalidRegistrationException("Password does not meet the security requirements.");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(Hasher.hashString(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setAddress(userDto.getAddress());
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Override
    public boolean isEmailEligibleForRegistration(String email) {
        return !userRepository.existsByEmail(email) && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    @Override
    public boolean isPhoneNumberEligibleForRegistration(String phoneNumber) {
        return phoneNumber.matches("^(\\+48)?\\d{9}$");
    }

    @Override
    public boolean isUsernameUniqueAndProper(String username) {
        return !userRepository.existsByUsername(username) && username.matches("^[a-zA-Z0-9._-]{5,20}$");
    }
}