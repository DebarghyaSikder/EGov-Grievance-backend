package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.CreateGrievanceRequest;
import com.grievance.grievance_service.dto.GrievanceResponse;
import com.grievance.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;

    /**
     * Citizen creates grievance (token required; userId forwarded from gateway)
     */
    @PostMapping
    public ResponseEntity<GrievanceResponse> createGrievance(
            @Valid @RequestBody CreateGrievanceRequest request,
            @RequestHeader("X-User-Id") Long citizenId
    ) {
        GrievanceResponse response = grievanceService.createGrievance(request, citizenId);
        return ResponseEntity.ok(response);
    }
}