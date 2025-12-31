package com.grievance.grievance_service.repository;

import com.grievance.grievance_service.entity.GrievanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceHistoryRepository extends JpaRepository<GrievanceHistory, Long> {

    List<GrievanceHistory> findByGrievanceIdOrderByTimestampAsc(Long grievanceId);
}