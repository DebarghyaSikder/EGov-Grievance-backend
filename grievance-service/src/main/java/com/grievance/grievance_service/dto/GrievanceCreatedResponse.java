package com.grievance.grievance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceCreatedResponse {

    private Long grievanceId;
    private String grievanceNumber;
    private String status;
    private Long assignedOfficerId;
}