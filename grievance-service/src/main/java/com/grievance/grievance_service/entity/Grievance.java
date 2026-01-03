package com.grievance.grievance_service.entity;

import com.grievance.grievance_service.enums.Priority;
import com.grievance.grievance_service.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grievances")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String grievanceNumber;

    private Long citizenId;

    private Long departmentId;
    
    private String departmentName;

    private Long categoryId;
    
    private String categoryName;

    private Long subCategoryId;
    
    private String subCategoryName;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long assignedOfficerId;

    private Integer slaHours;

    private LocalDateTime slaDeadline;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime assignedAt;

    @OneToMany(mappedBy = "grievance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GrievanceAttachment> attachments = new ArrayList<>();
}