package com.grievance.auth_service.service;

import com.grievance.auth_service.dto.AuthResponse;
import com.grievance.auth_service.dto.LoginRequest;
import com.grievance.auth_service.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
