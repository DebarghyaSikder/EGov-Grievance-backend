package com.grievance.grievance_service.repository;

import com.grievance.grievance_service.entity.GrievanceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceAttachmentRepository extends JpaRepository<GrievanceAttachment, Long> {

    List<GrievanceAttachment> findByGrievanceId(Long grievanceId);
}