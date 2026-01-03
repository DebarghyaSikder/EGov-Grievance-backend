package com.grievance.grievance_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "officer_departments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class OfficerDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long officerId;

    @Column(nullable = false)
    private Long departmentId;

    private Boolean isActive;

    private Integer currentLoad;

    @PrePersist
    public void onCreate() {
        if (isActive == null) isActive = true;
        if (currentLoad == null) currentLoad = 0;
    }
}