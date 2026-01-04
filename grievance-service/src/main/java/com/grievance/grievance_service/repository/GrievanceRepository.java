package com.grievance.grievance_service.repository;

import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    Optional<Grievance> findByGrievanceNumber(String grievanceNumber);

    List<Grievance> findByCitizenId(Long citizenId);

    List<Grievance> findByAssignedOfficerId(Long officerId);

    List<Grievance> findByDepartmentName(String departmentName);

    List<Grievance> findByStatus(Status status);

    List<Grievance> findByStatusInAndSlaDeadlineBefore(List<Status> statuses, LocalDateTime deadline);

    // Paginated versions
    Page<Grievance> findAll(Pageable pageable);

    Page<Grievance> findByCitizenId(Long citizenId, Pageable pageable);

    Page<Grievance> findByAssignedOfficerId(Long officerId, Pageable pageable);

    Page<Grievance> findByDepartmentName(String departmentName, Pageable pageable);

    Page<Grievance> findByStatus(Status status, Pageable pageable);

    @Query("SELECT g FROM Grievance g WHERE g.citizenId = :citizenId ORDER BY g.createdAt DESC")
    Page<Grievance> findByCitizenIdOrderByCreatedAtDesc(Long citizenId, Pageable pageable);
}