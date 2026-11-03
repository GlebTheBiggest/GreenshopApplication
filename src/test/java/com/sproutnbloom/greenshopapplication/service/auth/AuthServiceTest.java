package com.sproutnbloom.greenshopapplication.service.auth;

import com.sproutnbloom.greenshopapplication.enums.Role;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.repository.TokenRepository;
import com.sproutnbloom.greenshopapplication.repository.UserRepository;
import com.sproutnbloom.greenshopapplication.request.AuthRequest;
import com.sproutnbloom.greenshopapplication.request.RegisterRequest;
import com.sproutnbloom.greenshopapplication.response.AuthResponse;
import com.sproutnbloom.greenshopapplication.service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Companion to the AuthenticationIntegrationTest 401-vs-500 regression test: this one
// confirms AuthService itself never swallows the AuthenticationException - it must keep
// propagating so GlobalExceptionHandler can turn it into a clean 401.
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;

    @Test
    void register_encodesPasswordAndReturnsTokens() {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("Test").lastname("User")
                .email("test@test.com").password("raw-password")
                .build();
        User savedUser = User.builder().id(1L).email("test@test.com").role(Role.USER).build();
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(tokenRepository).save(argThat(token -> token.getToken().equals("access-token")));
    }

    @Test
    void authenticate_badCredentials_propagatesExceptionInsteadOfSwallowingIt() {
        AuthRequest request = AuthRequest.builder().email("bad@test.com").password("wrong").build();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));

        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_validCredentials_returnsTokens() {
        AuthRequest request = AuthRequest.builder().email("ok@test.com").password("right-password").build();
        User user = User.builder().id(1L).email("ok@test.com").role(Role.USER).build();
        when(userRepository.findByEmail("ok@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(tokenRepository.findAllValidTokenByUser(1L)).thenReturn(Collections.emptyList());

        AuthResponse response = authService.authenticate(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
