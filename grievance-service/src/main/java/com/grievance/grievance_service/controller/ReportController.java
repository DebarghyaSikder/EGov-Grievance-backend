package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.ApiResponse;
import com.grievance.grievance_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/grievances-by-status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getGrievancesByStatus(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getGrievanceCountByStatus()));
    }

    @GetMapping("/grievances-by-department")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getGrievancesByDepartment(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getGrievanceCountByDepartment()));
    }

    @GetMapping("/grievances-by-category")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getGrievancesByCategory(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getGrievanceCountByCategory()));
    }

    @GetMapping("/pending-vs-resolved")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingVsResolved(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getPendingVsResolved()));
    }

    @GetMapping("/average-resolution-time")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAverageResolutionTime(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getAverageResolutionTime()));
    }

    @GetMapping("/department-performance")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Object>>>> getDepartmentPerformance(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getDepartmentPerformance()));
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMonthlyTrends(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyTrends()));
    }

    @GetMapping("/officer-workload")
    public ResponseEntity<ApiResponse<Map<Long, Map<String, Object>>>> getOfficerWorkload(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        return ResponseEntity.ok(ApiResponse.success(reportService.getOfficerWorkload()));
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN", "DEPARTMENT_OFFICER");
        return ResponseEntity.ok(ApiResponse.success(reportService.getDashboardSummary()));
    }

    private void validateRole(String userRole, String... allowedRoles) {
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(userRole)) {
                return;
            }
        }
        throw new RuntimeException("Access denied. Required roles: " + String.join(", ", allowedRoles));
    }
}