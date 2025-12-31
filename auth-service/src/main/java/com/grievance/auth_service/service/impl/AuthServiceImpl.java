package com.grievance.auth_service.service.impl;

import com.grievance.auth_service.dto.AuthResponse;
import com.grievance.auth_service.dto.LoginRequest;
import com.grievance.auth_service.dto.RegisterRequest;
import com.grievance.auth_service.entity.Role;
import com.grievance.auth_service.entity.User;
import com.grievance.auth_service.repository.UserRepository;
import com.grievance.auth_service.service.AuthService;
import com.grievance.auth_service.service.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    

    @Override
    public AuthResponse register(RegisterRequest request) {

        // --- Validations (more soon as per SDD) ---
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already exists", null, null, null);
        }

        if (userRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            return new AuthResponse("Aadhaar already exists", null, null, null);
        }

        Role role = Role.CITIZEN; // Citizens always self-register

        // Create hashed user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .aadhaarNumber(request.getAadhaarNumber())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful")
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new AuthResponse("Invalid credentials", null, null, null);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse("Invalid credentials", null, null, null);
        }

        // generate token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .message("Login successful")
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

}
