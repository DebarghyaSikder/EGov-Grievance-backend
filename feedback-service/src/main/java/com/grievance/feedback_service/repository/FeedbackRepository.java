package com.grievance.feedback_service.repository;

import com.grievance.feedback_service.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByGrievanceId(Long grievanceId);

    List<Feedback> findByCitizenId(Long citizenId);

    boolean existsByGrievanceId(Long grievanceId);

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double findAverageRating();

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.grievanceId IN :grievanceIds")
    Double findAverageRatingByGrievanceIds(List<Long> grievanceIds);
}