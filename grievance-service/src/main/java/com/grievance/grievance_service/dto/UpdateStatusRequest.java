package com.grievance.grievance_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;
}