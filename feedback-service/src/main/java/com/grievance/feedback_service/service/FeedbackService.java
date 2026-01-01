package com.grievance.feedback_service.service;

import com.grievance.feedback_service.dto.CreateFeedbackRequest;
import com.grievance.feedback_service.dto.FeedbackResponse;

import java.util.List;

public interface FeedbackService {

    FeedbackResponse submitFeedback(CreateFeedbackRequest request, Long citizenId);

    FeedbackResponse getFeedbackByGrievanceId(Long grievanceId);

    List<FeedbackResponse> getFeedbackByCitizenId(Long citizenId);

    Double getAverageRating();
}