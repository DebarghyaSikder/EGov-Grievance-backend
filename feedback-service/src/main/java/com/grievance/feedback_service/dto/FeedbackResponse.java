package com.grievance.feedback_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private Long grievanceId;
    private Long citizenId;
    private Integer rating;
    private String comments;
    private Boolean reopenRequested;
    private String reopenReason;
    private LocalDateTime createdAt;
    private String message;
}