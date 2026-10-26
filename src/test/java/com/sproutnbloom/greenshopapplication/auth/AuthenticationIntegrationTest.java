package com.sproutnbloom.greenshopapplication.auth;

import com.sproutnbloom.greenshopapplication.AbstractIntegrationTest;
import com.sproutnbloom.greenshopapplication.request.AuthRequest;
import com.sproutnbloom.greenshopapplication.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void register_returnsAccessAndRefreshTokens() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("New").lastname("User")
                .email("newuser@test.com").password("Password1!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty());
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        registerAndGetToken("duplicate@test.com", "Password1!");

        RegisterRequest request = RegisterRequest.builder()
                .firstname("Another").lastname("User")
                .email("duplicate@test.com").password("Password1!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void authenticate_correctPassword_returnsTokens() throws Exception {
        registerAndGetToken("login@test.com", "Password1!");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AuthRequest.builder().email("login@test.com").password("Password1!").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    // Regression test for the AuthService 500 fix (see GlobalExceptionHandler):
    // a bad password must come back as 401, not an unhandled server error.
    @Test
    void authenticate_wrongPassword_returnsUnauthorized() throws Exception {
        registerAndGetToken("wrongpass@test.com", "Password1!");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AuthRequest.builder().email("wrongpass@test.com").password("NotThePassword!").build())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticate_unknownEmail_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AuthRequest.builder().email("nobody@test.com").password("Password1!").build())))
                .andExpect(status().isUnauthorized());
    }
}
