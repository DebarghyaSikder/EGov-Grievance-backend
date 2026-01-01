package com.grievance.grievance_service.entity;

import com.grievance.grievance_service.enums.Status;
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

    @Enumerated(EnumType.STRING)
    private Status oldStatus;

    @Enumerated(EnumType.STRING)
    private Status newStatus;

    private Long changedBy;

    private String remarks;

    private LocalDateTime changedAt;
}