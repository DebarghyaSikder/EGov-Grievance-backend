package com.grievance.feedback_service.controller;

import com.grievance.feedback_service.dto.CreateFeedbackRequest;
import com.grievance.feedback_service.dto.FeedbackResponse;
import com.grievance.feedback_service.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @Valid @RequestBody CreateFeedbackRequest request,
            @RequestHeader("X-User-Id") Long citizenId
    ) {
        FeedbackResponse response = feedbackService.submitFeedback(request, citizenId);
        
        Map<String, Object> result = Map.of(
                "success", true,
                "message", response.getMessage() != null ? response.getMessage() : "Feedback submitted successfully",
                "feedbackId", response.getId() != null ? response.getId() : 0
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/grievance/{grievanceId}")
    public ResponseEntity<FeedbackResponse> getFeedbackByGrievanceId(@PathVariable Long grievanceId) {
        FeedbackResponse response = feedbackService.getFeedbackByGrievanceId(grievanceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedbacks(@RequestHeader("X-User-Id") Long citizenId) {
        List<FeedbackResponse> responses = feedbackService.getFeedbackByCitizenId(citizenId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@RequestHeader("X-User-Role") String role) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        Double avgRating = feedbackService.getAverageRating();
        
        Map<String, Object> result = Map.of(
                "success", true,
                "averageRating", avgRating
        );
        
        return ResponseEntity.ok(result);
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