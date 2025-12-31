package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.CreateGrievanceRequest;
import com.grievance.grievance_service.dto.GrievanceResponse;
import com.grievance.grievance_service.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;

    /**
     * Citizen creates grievance (token required)
     */
    @PostMapping
    public ResponseEntity<GrievanceResponse> createGrievance(
            @Valid @RequestBody CreateGrievanceRequest request,
            Authentication authentication
    ) {
        Long citizenId = extractCitizenId(authentication);
        GrievanceResponse response = grievanceService.createGrievance(request, citizenId);
        return ResponseEntity.ok(response);
    }


    /**
     * Utility — extract citizenId from principal
     */
    private Long extractCitizenId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication token missing");
        }

        try {
            String principal = authentication.getPrincipal().toString();
            return Long.parseLong(principal);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Invalid authenticated principal format. Expected numeric userId."
            );
        }
    }
}