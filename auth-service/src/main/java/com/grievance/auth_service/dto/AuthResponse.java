package com.grievance.auth_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String message;
    private String token; // temporarily null until we implement JWT
    private String role;
    private Long userId;
}