package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;

    @PostMapping
    public ResponseEntity<GrievanceResponse> createGrievance(
            @Valid @RequestBody CreateGrievanceRequest request,
            @RequestHeader("X-User-Id") Long citizenId
    ) {
        GrievanceResponse response = grievanceService.createGrievance(request, citizenId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrievanceResponse> getGrievanceById(@PathVariable Long id) {
        Grievance grievance = grievanceService.getGrievanceById(id);
        return ResponseEntity.ok(mapToResponse(grievance));
    }

    @GetMapping("/tracking/{grievanceNumber}")
    public ResponseEntity<GrievanceResponse> getByGrievanceNumber(@PathVariable String grievanceNumber) {
        Grievance grievance = grievanceService.getByGrievanceNumber(grievanceNumber);
        return ResponseEntity.ok(mapToResponse(grievance));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GrievanceResponse>> getMyGrievances(@RequestHeader("X-User-Id") Long citizenId) {
        List<Grievance> grievances = grievanceService.getGrievancesByCitizenId(citizenId);
        return ResponseEntity.ok(grievances.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<GrievanceResponse>> getByDepartment(
            @PathVariable String department,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER", "DEPARTMENT_OFFICER");
        List<Grievance> grievances = grievanceService.getGrievancesByDepartment(department);
        return ResponseEntity.ok(grievances.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/officer/assigned")
    public ResponseEntity<List<GrievanceResponse>> getAssignedGrievances(
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER");
        List<Grievance> grievances = grievanceService.getGrievancesByOfficerId(officerId);
        return ResponseEntity.ok(grievances.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/all")
    public ResponseEntity<List<GrievanceResponse>> getAllGrievances(
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        List<Grievance> grievances = grievanceService.getAllGrievances();
        return ResponseEntity.ok(grievances.stream().map(this::mapToResponse).toList());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<GrievanceResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "DEPARTMENT_OFFICER", "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        Grievance grievance = grievanceService.updateStatus(id, request, officerId);
        return ResponseEntity.ok(mapToResponse(grievance));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<GrievanceResponse> assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        Grievance grievance = grievanceService.assignOfficer(id, request);
        return ResponseEntity.ok(mapToResponse(grievance));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GrievanceResponse>> getByStatus(
            @PathVariable String status,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER", "DEPARTMENT_OFFICER");
        List<Grievance> grievances = grievanceService.getGrievancesByStatus(status);
        return ResponseEntity.ok(grievances.stream().map(this::mapToResponse).toList());
    }
    
    @PutMapping("/{id}/escalate")
    public ResponseEntity<GrievanceResponse> escalateGrievance(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        String reason = request.get("reason");
        Grievance grievance = grievanceService.escalateGrievance(id, reason);
        return ResponseEntity.ok(mapToResponse(grievance));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<GrievanceResponse> reassignGrievance(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        validateRole(role, "SUPERVISORY_OFFICER", "SYSTEM_ADMIN");
        Grievance grievance = grievanceService.reassignOfficer(id, request);
        return ResponseEntity.ok(mapToResponse(grievance));
    }

    private void validateRole(String userRole, String... allowedRoles) {
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(userRole)) {
                return;
            }
        }
        throw new RuntimeException("Access denied. Required roles: " + String.join(", ", allowedRoles));
    }

    private GrievanceResponse mapToResponse(Grievance grievance) {
        return GrievanceResponse.builder()
                .grievanceId(grievance.getId())
                .grievanceNumber(grievance.getGrievanceNumber())
                .citizenId(grievance.getCitizenId())
                .title(grievance.getTitle())
                .description(grievance.getDescription())
                .category(grievance.getCategory())
                .department(grievance.getDepartment())
                .priority(grievance.getPriority() != null ? grievance.getPriority().name() : null)
                .status(grievance.getStatus() != null ? grievance.getStatus().name() : null)
                .createdAt(grievance.getCreatedAt())
                .updatedAt(grievance.getUpdatedAt())
                .build();
    }
}