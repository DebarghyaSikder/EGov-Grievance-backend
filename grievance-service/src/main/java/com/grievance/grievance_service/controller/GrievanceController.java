package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.exception.AccessDeniedException;
import com.grievance.grievance_service.scheduler.EscalationScheduler;
import com.grievance.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class GrievanceController {

	@Autowired
	private EscalationScheduler escalationScheduler;
    private final GrievanceService grievanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<GrievanceCreatedResponse>> createGrievance(
            @Valid @RequestBody CreateGrievanceRequest request,
            @RequestHeader("X-User-Id") Long citizenId
    ) {
        GrievanceCreatedResponse response = grievanceService.createGrievance(request, citizenId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grievance created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GrievanceResponse>> getGrievanceById(@PathVariable Long id) {
        Grievance grievance = grievanceService.getGrievanceById(id);
        return ResponseEntity.ok(ApiResponse.success("Grievance retrieved successfully", mapToResponse(grievance)));
    }

    @GetMapping("/tracking/{grievanceNumber}")
    public ResponseEntity<ApiResponse<GrievanceResponse>> getByGrievanceNumber(@PathVariable String grievanceNumber) {
        Grievance grievance = grievanceService.getByGrievanceNumber(grievanceNumber);
        return ResponseEntity.ok(ApiResponse.success("Grievance retrieved successfully", mapToResponse(grievance)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getMyGrievances(@RequestHeader("X-User-Id") Long citizenId) {
        List<Grievance> grievances = grievanceService.getGrievancesByCitizenId(citizenId);
        List<GrievanceResponse> responses = grievances.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("Grievances retrieved successfully", responses));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getByDepartment(
            @PathVariable String department,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER", "DEPARTMENT_OFFICER");
        List<Grievance> grievances = grievanceService.getGrievancesByDepartment(department);
        List<GrievanceResponse> responses = grievances.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("Grievances retrieved successfully", responses));
    }

    @GetMapping("/officer/assigned")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getAssignedGrievances(
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER");
        List<Grievance> grievances = grievanceService.getGrievancesByOfficerId(officerId);
        List<GrievanceResponse> responses = grievances.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("Assigned grievances retrieved successfully", responses));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getAllGrievances(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        List<Grievance> grievances = grievanceService.getAllGrievances();
        List<GrievanceResponse> responses = grievances.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("All grievances retrieved successfully", responses));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        Grievance grievance = grievanceService.updateStatus(id, request, officerId);
        
        Map<String, Object> data = Map.of(
                "grievanceId", grievance.getId(),
                "grievanceNumber", grievance.getGrievanceNumber(),
                "newStatus", grievance.getStatus().name()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", data));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        Grievance grievance = grievanceService.assignOfficer(id, request);
        
        Map<String, Object> data = Map.of(
                "grievanceId", grievance.getId(),
                "grievanceNumber", grievance.getGrievanceNumber(),
                "assignedOfficerId", grievance.getAssignedOfficerId(),
                "status", grievance.getStatus().name()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Officer assigned successfully", data));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<GrievanceResponse>>> getByStatus(
            @PathVariable String status,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER", "DEPARTMENT_OFFICER");
        List<Grievance> grievances = grievanceService.getGrievancesByStatus(status);
        List<GrievanceResponse> responses = grievances.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("Grievances retrieved successfully", responses));
    }

    @PutMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> escalateGrievance(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        String reason = request.get("reason");
        Grievance grievance = grievanceService.escalateGrievance(id, reason);
        
        Map<String, Object> data = Map.of(
                "grievanceId", grievance.getId(),
                "grievanceNumber", grievance.getGrievanceNumber(),
                "status", grievance.getStatus().name()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Grievance escalated successfully", data));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reassignGrievance(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.reassignOfficer(id, request);
        
        Map<String, Object> data = Map.of(
                "grievanceId", grievance.getId(),
                "grievanceNumber", grievance.getGrievanceNumber(),
                "newOfficerId", grievance.getAssignedOfficerId(),
                "status", grievance.getStatus().name()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Grievance reassigned successfully", data));
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
        throw new AccessDeniedException(allowedRoles);
    }

    private GrievanceResponse mapToResponse(Grievance grievance) {
        return GrievanceResponse.builder()
                .grievanceId(grievance.getId())
                .grievanceNumber(grievance.getGrievanceNumber())
                .citizenId(grievance.getCitizenId())
                .title(grievance.getTitle())
                .description(grievance.getDescription())
                .departmentId(grievance.getDepartmentId())
                .departmentName(grievance.getDepartmentName())
                .categoryId(grievance.getCategoryId())
                .categoryName(grievance.getCategoryName())
                .subCategoryId(grievance.getSubCategoryId())
                .subCategoryName(grievance.getSubCategoryName())
                .priority(grievance.getPriority() != null ? grievance.getPriority().name() : null)
                .status(grievance.getStatus() != null ? grievance.getStatus().name() : null)
                .assignedOfficerId(grievance.getAssignedOfficerId())
                .slaHours(grievance.getSlaHours())
                .slaDeadline(grievance.getSlaDeadline())
                .createdAt(grievance.getCreatedAt())
                .updatedAt(grievance.getUpdatedAt())
                .assignedAt(grievance.getAssignedAt())
                .build();
    }
}