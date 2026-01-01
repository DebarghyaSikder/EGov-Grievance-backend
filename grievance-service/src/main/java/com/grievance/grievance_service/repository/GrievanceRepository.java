package com.grievance.grievance_service.repository;

import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    Optional<Grievance> findByGrievanceNumber(String grievanceNumber);

    List<Grievance> findByCitizenId(Long citizenId);

    List<Grievance> findByDepartment(String department);

    List<Grievance> findByAssignedOfficerId(Long officerId);

    List<Grievance> findByStatus(Status status);
}