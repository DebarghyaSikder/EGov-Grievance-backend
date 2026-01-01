package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Grievance> getGrievanceById(@PathVariable Long id) {
        Grievance grievance = grievanceService.getGrievanceById(id);
        return ResponseEntity.ok(grievance);
    }

    @GetMapping("/tracking/{grievanceNumber}")
    public ResponseEntity<Grievance> getByGrievanceNumber(@PathVariable String grievanceNumber) {
        Grievance grievance = grievanceService.getByGrievanceNumber(grievanceNumber);
        return ResponseEntity.ok(grievance);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Grievance>> getMyGrievances(@RequestHeader("X-User-Id") Long citizenId) {
        List<Grievance> grievances = grievanceService.getGrievancesByCitizenId(citizenId);
        return ResponseEntity.ok(grievances);
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<Grievance>> getByDepartment(@PathVariable String department) {
        List<Grievance> grievances = grievanceService.getGrievancesByDepartment(department);
        return ResponseEntity.ok(grievances);
    }

    @GetMapping("/officer/assigned")
    public ResponseEntity<List<Grievance>> getAssignedGrievances(@RequestHeader("X-User-Id") Long officerId) {
        List<Grievance> grievances = grievanceService.getGrievancesByOfficerId(officerId);
        return ResponseEntity.ok(grievances);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Grievance>> getAllGrievances() {
        List<Grievance> grievances = grievanceService.getAllGrievances();
        return ResponseEntity.ok(grievances);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Grievance> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Id") Long officerId
    ) {
        Grievance grievance = grievanceService.updateStatus(id, request, officerId);
        return ResponseEntity.ok(grievance);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Grievance> assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AssignOfficerRequest request
    ) {
        Grievance grievance = grievanceService.assignOfficer(id, request);
        return ResponseEntity.ok(grievance);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Grievance>> getByStatus(@PathVariable String status) {
        List<Grievance> grievances = grievanceService.getGrievancesByStatus(status);
        return ResponseEntity.ok(grievances);
    }
}