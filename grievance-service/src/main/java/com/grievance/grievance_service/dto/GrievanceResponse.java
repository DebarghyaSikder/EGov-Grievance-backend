package com.grievance.grievance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceResponse {

    private Long grievanceId;
    private String message;
    private String grievanceNumber;
    private Long citizenId;
    private String title;
    private String description;
    private Long departmentId;
    private String departmentName;
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private String priority;
    private String status;
    private Long assignedOfficerId;
    private Integer slaHours;
    private LocalDateTime slaDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assignedAt;
}