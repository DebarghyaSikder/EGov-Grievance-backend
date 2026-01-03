package com.grievance.api_gateway.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class RouteValidator {

    // Public endpoints - no authentication required
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login"
    );

    // Endpoint patterns with allowed roles
    private static final List<EndpointRole> ENDPOINT_ROLES = List.of(
            // CITIZEN endpoints
            new EndpointRole("POST", "/api/v1/grievances", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/my", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/tracking/.*", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("POST", "/api/v1/grievances/\\d+/attachments", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/\\d+/attachments.*", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("DELETE", "/api/v1/grievances/\\d+/attachments/\\d+", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("POST", "/api/v1/feedbacks", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/feedbacks/my", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/feedbacks/grievance/.*", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/notifications/my", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/notifications/unread.*", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/notifications/\\d+/read", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/notifications/mark-all-read", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            // DEPARTMENT_OFFICER endpoints
            new EndpointRole("GET", "/api/v1/grievances/officer/assigned", List.of("DEPARTMENT_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/status", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/department/.*", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/status/.*", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            // SUPERVISORY_OFFICER endpoints
            new EndpointRole("GET", "/api/v1/grievances/all", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/assign", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/escalate", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/reassign", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/feedbacks/average-rating", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("POST", "/api/v1/grievances/admin/trigger-escalation", List.of("SYSTEM_ADMIN")),

            // Common GET endpoints - all authenticated users
            new EndpointRole("GET", "/api/v1/grievances/\\d+", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN"))
    );

    public boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::equals);
    }

    public boolean hasAccess(String role, String method, String path) {
        // SYSTEM_ADMIN has full access
        if ("SYSTEM_ADMIN".equals(role)) {
            return true;
        }

        for (EndpointRole endpointRole : ENDPOINT_ROLES) {
            if (endpointRole.matches(method, path) && endpointRole.allowedRoles().contains(role)) {
                return true;
            }
        }

        return false;
    }

    private record EndpointRole(String method, String pathPattern, List<String> allowedRoles) {
        public boolean matches(String reqMethod, String reqPath) {
            if (!this.method.equalsIgnoreCase(reqMethod)) {
                return false;
            }
            return Pattern.matches(this.pathPattern, reqPath);
        }
    }
}