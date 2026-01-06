package com.grievance.auth_service.service;

import com.grievance.auth_service.dto.AuthResponse;
import com.grievance.auth_service.dto.RegisterRequest;
import com.grievance.auth_service.entity.User;
import com.grievance.auth_service.entity.Role;
import com.grievance.auth_service.repository.UserRepository;
import com.grievance.auth_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("9876543210");
        registerRequest.setAadhaarNumber("123456789012");
        registerRequest.setRole(Role.CITIZEN);

        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@test.com")
                .password("encodedPassword")
                .phone("9876543210")
                .aadhaarNumber("123456789012")
                .role(Role.CITIZEN)
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByAadhaarNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());
        assertEquals(1L, response.getUserId());
        assertEquals("CITIZEN", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ReturnsError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Email already exists", response.getMessage());
        assertNull(response.getUserId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_AadhaarAlreadyExists_ReturnsError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByAadhaarNumber(anyString())).thenReturn(true);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Aadhaar already exists", response.getMessage());
        assertNull(response.getUserId());
        verify(userRepository, never()).save(any(User.class));
    }
}