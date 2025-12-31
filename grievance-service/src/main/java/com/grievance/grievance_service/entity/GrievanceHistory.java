package com.grievance.grievance_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_history")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class GrievanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long grievanceId;
    private Long actionByUserId;

    private String actionType;         // CREATED, ASSIGNED, etc.
    private String prevStatus;
    private String newStatus;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Long assignedTo;

    private LocalDateTime timestamp;

    @PrePersist
    public void onCreate() {
        timestamp = LocalDateTime.now();
    }
}