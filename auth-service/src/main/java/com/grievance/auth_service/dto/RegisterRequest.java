package com.grievance.auth_service.dto;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be 12 digits")
    @NotBlank(message = "Aadhaar number is required")
    private String aadhaarNumber;

    @Size(min = 8, max = 20, message = "Password must be between 8–20 characters")
    @NotBlank(message = "Password is required")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    @NotBlank(message = "Phone is required")
    private String phone;
}