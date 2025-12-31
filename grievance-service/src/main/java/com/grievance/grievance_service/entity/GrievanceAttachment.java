package com.grievance.grievance_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_attachment")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class GrievanceAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long grievanceId;
    private String fileName;
    private String fileType;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;

    private Long uploadedBy;
    private LocalDateTime uploadedAt;

    @PrePersist
    private void onUpload() {
        uploadedAt = LocalDateTime.now();
    }
}