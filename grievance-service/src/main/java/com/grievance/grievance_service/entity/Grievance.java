package com.grievance.grievance_service.entity;

import com.grievance.grievance_service.enums.Priority;
import com.grievance.grievance_service.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grievance")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String grievanceNumber; // GRV-202501-0001 style

    private Long citizenId;

    private String department;

    private String category;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long assignedOfficerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        status = Status.PENDING;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}