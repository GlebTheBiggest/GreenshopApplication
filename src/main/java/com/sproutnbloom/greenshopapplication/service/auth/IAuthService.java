package com.sproutnbloom.greenshopapplication.service.auth;

import com.sproutnbloom.greenshopapplication.request.RegisterRequest;
import com.sproutnbloom.greenshopapplication.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.sproutnbloom.greenshopapplication.request.AuthRequest;

import java.io.IOException;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(AuthRequest request);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
