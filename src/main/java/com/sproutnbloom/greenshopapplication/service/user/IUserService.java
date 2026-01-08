package com.sproutnbloom.greenshopapplication.service.user;

import com.sproutnbloom.greenshopapplication.dto.UserDto;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.request.CreateUserRequest;
import com.sproutnbloom.greenshopapplication.request.UpdateUserRequest;

public interface IUserService {

    UserDto getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UpdateUserRequest request, Long userId);
    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);
}
