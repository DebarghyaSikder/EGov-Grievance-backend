package com.grievance.grievance_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGrievanceStatusRequest {

    @NotBlank(message = "Status must not be empty")
    private String status;

    private String officerRemark;
}