package com.grievance.grievance_service.dto;

import com.grievance.grievance_service.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status must be provided")
    private Status status;

    private String comment; // optional
}