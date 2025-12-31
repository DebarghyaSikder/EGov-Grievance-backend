package com.grievance.grievance_service.dto;

import com.grievance.grievance_service.enums.Priority;
import com.grievance.grievance_service.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GrievanceResponse {

    private String grievanceNumber;
    private Long citizenId;
    private Long assignedOfficerId;

    private String title;
    private String description;

    private String department;
    private String category;

    private Priority priority;
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}