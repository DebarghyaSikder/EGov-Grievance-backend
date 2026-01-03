package com.grievance.grievance_service.dto;

import com.grievance.grievance_service.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGrievanceRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Department name is required")
    private String departmentName;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    @NotNull(message = "Sub-category ID is required")
    private Long subCategoryId;

    @NotBlank(message = "Sub-category name is required")
    private String subCategoryName;

    @NotNull(message = "SLA hours is required")
    private Integer slaHours;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Priority priority;
}