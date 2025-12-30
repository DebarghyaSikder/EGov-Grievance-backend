package com.grievance.auth_service.dto;

import com.grievance.auth_service.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String fullName;
    private String email;
    private String aadhaarNumber;
    private String password;
    private String phone;
    private Role role;   // will default to CITIZEN in controller for self-registration
}