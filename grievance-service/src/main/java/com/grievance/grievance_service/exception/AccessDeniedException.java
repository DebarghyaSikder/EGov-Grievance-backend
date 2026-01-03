package com.grievance.grievance_service.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String... allowedRoles) {
        super("Access denied. Required roles: " + String.join(", ", allowedRoles));
    }
}