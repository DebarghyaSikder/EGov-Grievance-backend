package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.scheduler.EscalationScheduler;
import com.grievance.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;
    private final EscalationScheduler escalationScheduler;

    @PostMapping
    public ResponseEntity<ApiResponse<GrievanceCreatedResponse>> createGrievance(
            @Valid @RequestBody CreateGrievanceRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "CITIZEN", "SYSTEM_ADMIN");
        GrievanceCreatedResponse response = grievanceService.createGrievance(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grievance created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Grievance>> getGrievanceById(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        Grievance grievance = grievanceService.getGrievanceById(id);
        return ResponseEntity.ok(ApiResponse.success(grievance));
    }

    @GetMapping("/tracking/{grievanceNumber}")
    public ResponseEntity<ApiResponse<Grievance>> trackGrievance(
            @PathVariable String grievanceNumber,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "CITIZEN", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.getByGrievanceNumber(grievanceNumber);
        return ResponseEntity.ok(ApiResponse.success(grievance));
    }

    // Non-paginated (for backward compatibility)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Grievance>>> getMyGrievances(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "CITIZEN", "SYSTEM_ADMIN");
        List<Grievance> grievances = grievanceService.getGrievancesByCitizenId(userId);
        return ResponseEntity.ok(ApiResponse.success(grievances));
    }

    // Paginated version
    @GetMapping("/my/paged")
    public ResponseEntity<ApiResponse<PageResponse<Grievance>>> getMyGrievancesPaged(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        validateRole(role, "CITIZEN", "SYSTEM_ADMIN");

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Grievance> grievancePage = grievanceService.getGrievancesByCitizenId(userId, pageable);
        PageResponse<Grievance> response = buildPageResponse(grievancePage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/officer/assigned")
    public ResponseEntity<ApiResponse<List<Grievance>>> getAssignedGrievances(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SYSTEM_ADMIN");
        List<Grievance> grievances = grievanceService.getGrievancesByOfficerId(userId);
        return ResponseEntity.ok(ApiResponse.success(grievances));
    }

    @GetMapping("/officer/assigned/paged")
    public ResponseEntity<ApiResponse<PageResponse<Grievance>>> getAssignedGrievancesPaged(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SYSTEM_ADMIN");

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Grievance> grievancePage = grievanceService.getGrievancesByOfficerId(userId, pageable);
        PageResponse<Grievance> response = buildPageResponse(grievancePage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Grievance>>> getAllGrievances(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        List<Grievance> grievances = grievanceService.getAllGrievances();
        return ResponseEntity.ok(ApiResponse.success(grievances));
    }

    @GetMapping("/all/paged")
    public ResponseEntity<ApiResponse<PageResponse<Grievance>>> getAllGrievancesPaged(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Grievance> grievancePage = grievanceService.getAllGrievances(pageable);
        PageResponse<Grievance> response = buildPageResponse(grievancePage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Grievance>>> getByStatus(
            @PathVariable String status,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        List<Grievance> grievances = grievanceService.getGrievancesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(grievances));
    }

    @GetMapping("/status/{status}/paged")
    public ResponseEntity<ApiResponse<PageResponse<Grievance>>> getByStatusPaged(
            @PathVariable String status,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Grievance> grievancePage = grievanceService.getGrievancesByStatus(status, pageable);
        PageResponse<Grievance> response = buildPageResponse(grievancePage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<Grievance>>> getByDepartment(
            @PathVariable String department,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        List<Grievance> grievances = grievanceService.getGrievancesByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(grievances));
    }

    @GetMapping("/department/{department}/paged")
    public ResponseEntity<ApiResponse<PageResponse<Grievance>>> getByDepartmentPaged(
            @PathVariable String department,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Grievance> grievancePage = grievanceService.getGrievancesByDepartment(department, pageable);
        PageResponse<Grievance> response = buildPageResponse(grievancePage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Grievance>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.updateStatus(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", grievance));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Grievance>> assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.assignOfficer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Officer assigned successfully", grievance));
    }

    @PutMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<Grievance>> escalateGrievance(
            @PathVariable Long id,
            @RequestBody EscalateRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.escalateGrievance(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Grievance escalated successfully", grievance));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<ApiResponse<Grievance>> reassignGrievance(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.reassignOfficer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grievance reassigned successfully", grievance));
    }

    @PostMapping("/admin/trigger-escalation")
    public ResponseEntity<ApiResponse<String>> triggerEscalation(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN");
        escalationScheduler.checkAndEscalateGrievances();
        return ResponseEntity.ok(ApiResponse.success("Escalation check triggered manually"));
    }

    private void validateRole(String userRole, String... allowedRoles) {
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(userRole)) {
                return;
            }
        }
        throw new RuntimeException("Access denied. Required roles: " + String.join(", ", allowedRoles));
    }

    private <T> PageResponse<T> buildPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}