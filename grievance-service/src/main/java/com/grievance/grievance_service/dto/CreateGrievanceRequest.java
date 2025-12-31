package com.grievance.grievance_service.dto;

import com.grievance.grievance_service.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGrievanceRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotBlank(message = "Department is required")
    private String department;

    private String category; // optional

    @NotNull(message = "Priority is required")
    private Priority priority;
}