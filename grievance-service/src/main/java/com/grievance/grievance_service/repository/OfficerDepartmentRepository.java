package com.grievance.grievance_service.repository;

import com.grievance.grievance_service.entity.OfficerDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficerDepartmentRepository extends JpaRepository<OfficerDepartment, Long> {

    @Query("SELECT od FROM OfficerDepartment od WHERE od.departmentId = :departmentId AND od.isActive = true ORDER BY od.currentLoad ASC")
    List<OfficerDepartment> findByDepartmentIdOrderByLoadAsc(Long departmentId);

    // One officer belongs to only one department, so this returns single result
    Optional<OfficerDepartment> findByOfficerId(Long officerId);

    List<OfficerDepartment> findByDepartmentIdAndIsActiveTrue(Long departmentId);
}