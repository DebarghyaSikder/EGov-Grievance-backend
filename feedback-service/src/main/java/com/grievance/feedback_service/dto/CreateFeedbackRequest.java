package com.grievance.feedback_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateFeedbackRequest {

    @NotNull(message = "Grievance ID is required")
    private Long grievanceId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comments;

    private Boolean reopenRequested;

    private String reopenReason;
}