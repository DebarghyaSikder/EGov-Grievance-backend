package com.grievance.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceEvent implements Serializable {

    private Long grievanceId;
    private String grievanceNumber;
    private Long citizenId;
    private String citizenEmail;
    private String title;
    private String department;
    private String category;
    private String oldStatus;
    private String newStatus;
    private String eventType;
    private String remarks;
}