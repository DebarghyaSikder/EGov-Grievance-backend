package com.grievance.grievance_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_attachments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class GrievanceAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grievance_id", nullable = false)
    @JsonIgnore  // Add this to prevent circular reference
    private Grievance grievance;

    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}