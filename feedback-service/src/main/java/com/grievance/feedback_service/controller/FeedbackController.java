package com.grievance.feedback_service.controller;

import com.grievance.feedback_service.dto.CreateFeedbackRequest;
import com.grievance.feedback_service.dto.FeedbackResponse;
import com.grievance.feedback_service.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody CreateFeedbackRequest request,
            @RequestHeader("X-User-Id") Long citizenId
    ) {
        FeedbackResponse response = feedbackService.submitFeedback(request, citizenId);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<Double> getAverageRating(@RequestHeader("X-User-Role") String role) {
        validateRole(role, "SYSTEM_ADMIN", "SUPERVISORY_OFFICER");
        Double avgRating = feedbackService.getAverageRating();
        return ResponseEntity.ok(avgRating);
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