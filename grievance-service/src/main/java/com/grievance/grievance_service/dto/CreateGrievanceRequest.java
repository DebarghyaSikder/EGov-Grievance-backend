package com.grievance.grievance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGrievanceRequest {

    @NotBlank(message = "Grievance title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category; // ex: Water Supply, Electricity

    @NotBlank(message = "Department assignment hint is required")
    private String departmentHint; // supports future AI mapping
}