package com.grievance.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RouteValidator {
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login");
    private static final List<EndpointRole> ENDPOINT_ROLES = List.of(
            /* AUTH */
            new EndpointRole("GET", "/api/v1/auth/me", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            /*  CITIZEN */
            new EndpointRole("POST", "/api/v1/grievances", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/my", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/tracking/.*", List.of("CITIZEN", "SYSTEM_ADMIN")),

            /* ATTACHMENTS*/
            new EndpointRole("POST", "/api/v1/grievances/\\d+/attachments", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/\\d+/attachments", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/\\d+/attachments/\\d+/download", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("DELETE", "/api/v1/grievances/\\d+/attachments/\\d+", List.of("CITIZEN", "SYSTEM_ADMIN")),

            /* FEEDBACK */
            new EndpointRole("POST", "/api/v1/feedbacks", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/feedbacks/my", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/feedbacks/grievance/.*", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/feedbacks/average-rating", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            /* NOTIFICATIONS */
            new EndpointRole("GET", "/api/v1/notifications/my", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/notifications/unread", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/notifications/unread-count", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/notifications/\\d+/read", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/notifications/mark-all-read", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            /* OFFICER */
            new EndpointRole("GET", "/api/v1/grievances/officer/assigned", List.of("DEPARTMENT_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/status", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/department/.*", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/status/.*", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            /* SUPERVISOR= */
            new EndpointRole("GET", "/api/v1/grievances/all", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/assign", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/escalate", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("PUT", "/api/v1/grievances/\\d+/reassign", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),

            /* ADMIN */
            new EndpointRole("POST", "/api/v1/grievances/admin/trigger-escalation", List.of("SYSTEM_ADMIN")),

            /* REPORTS */
            new EndpointRole("GET", "/api/v1/reports/grievances-by-status", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/grievances-by-department", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/grievances-by-category", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/pending-vs-resolved", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/average-resolution-time", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/department-performance", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/monthly-trends", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/officer-workload", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/reports/dashboard-summary", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            
            /* PAGINATED ENDPOINTS */
            new EndpointRole("GET", "/api/v1/grievances/my/paged", List.of("CITIZEN", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/officer/assigned/paged", List.of("DEPARTMENT_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/all/paged", List.of("SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/status/.+/paged", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            new EndpointRole("GET", "/api/v1/grievances/department/.+/paged", List.of("DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN")),
            
            /* COMMON */
            new EndpointRole("GET", "/api/v1/grievances/\\d+", List.of("CITIZEN", "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN"))
    );
    public boolean isPreflightRequest(HttpMethod method) {
        boolean result = method == HttpMethod.OPTIONS;
        if (result) {
            log.info("CORS preflight OPTIONS request detected - allowing through");
        }
        return result;
    }

    public boolean isPublicEndpoint(String path) {
        boolean result = PUBLIC_ENDPOINTS.stream().anyMatch(path::equals);
        log.info("Public endpoint check for [{}] => {}", path, result);
        return result;
    }

    public boolean hasAccess(String role, String method, String path) {
        log.info("RBAC check: role={}, method={}, path={}", role, method, path);

        if ("SYSTEM_ADMIN".equalsIgnoreCase(role)) {
            log.info("SYSTEM_ADMIN - full access granted");
            return true;
        }
        for (EndpointRole er : ENDPOINT_ROLES) {
            if (er.matches(method, path)) {
                boolean hasRole = er.allowedRoles.contains(role);
                log.info("Pattern [{}] matched. Role {} allowed: {}", er.pathPattern.pattern(), role, hasRole);
                if (hasRole) {
                    return true;
                }
            }
        }

        log.warn("No access granted for role={}, method={}, path={}", role, method, path);
        return false;
    }
    private static class EndpointRole {

        private final String method;
        private final Pattern pathPattern;
        private final List<String> allowedRoles;

        EndpointRole(String method, String regex, List<String> allowedRoles) {
            this.method = method;
            this.pathPattern = Pattern.compile("^" + regex + "$");
            this.allowedRoles = allowedRoles;
        }

        boolean matches(String reqMethod, String reqPath) {
            return method.equalsIgnoreCase(reqMethod)
                    && pathPattern.matcher(reqPath).matches();
        }
    }
}