package com.sproutnbloom.greenshopapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sproutnbloom.greenshopapplication.request.AuthRequest;
import com.sproutnbloom.greenshopapplication.request.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Singleton container pattern: started once, manually, in a static initializer and never
// stopped explicitly. Letting the JUnit @Testcontainers/@Container lifecycle manage this
// field instead would stop the shared container after the FIRST test class finishes
// (each subclass gets its own extension instance), leaving every later class unable to
// connect. Testcontainers' Ryuk reaper still cleans it up when the JVM exits.
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    static {
        postgres.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String registerAndGetToken(String email, String password) throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email(email)
                .password(password)
                .build();
        return extractAccessToken(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    protected String authenticateAndGetToken(String email, String password) throws Exception {
        AuthRequest request = AuthRequest.builder().email(email).password(password).build();
        return extractAccessToken(mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private String extractAccessToken(String jsonResponse) throws Exception {
        return objectMapper.readTree(jsonResponse).get("access_token").asText();
    }
}
