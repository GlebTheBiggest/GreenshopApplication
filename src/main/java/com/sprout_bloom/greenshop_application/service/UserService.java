package com.sprout_bloom.greenshop_application.service;

import com.sprout_bloom.greenshop_application.dto.UserRegistrationDto;
import com.sprout_bloom.greenshop_application.entity.User;

public interface UserService {

    User registerUser(UserRegistrationDto userDto);

    boolean isEmailEligibleForRegistration(String email);

    boolean isPhoneNumberEligibleForRegistration(String phoneNumber);

    boolean isUsernameUniqueAndProper(String username);
}