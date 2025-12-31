package com.grievance.grievance_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GrievanceResponse {
    private Long grievanceId;
    private Long citizenId;
    private String title;
    private String description;
    private String category;
    private String department;
    private String priority; 
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}