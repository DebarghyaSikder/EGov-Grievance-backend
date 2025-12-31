package com.grievance.grievance_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignOfficerRequest {

    @NotNull(message = "Officer ID must be provided")
    private Long officerId;
}