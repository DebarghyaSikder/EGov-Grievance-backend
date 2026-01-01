package com.grievance.feedback_service.service.impl;

import com.grievance.feedback_service.dto.CreateFeedbackRequest;
import com.grievance.feedback_service.dto.FeedbackResponse;
import com.grievance.feedback_service.entity.Feedback;
import com.grievance.feedback_service.repository.FeedbackRepository;
import com.grievance.feedback_service.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Override
    public FeedbackResponse submitFeedback(CreateFeedbackRequest request, Long citizenId) {
        if (feedbackRepository.existsByGrievanceId(request.getGrievanceId())) {
            return FeedbackResponse.builder()
                    .message("Feedback already submitted for this grievance")
                    .build();
        }

        Feedback feedback = Feedback.builder()
                .grievanceId(request.getGrievanceId())
                .citizenId(citizenId)
                .rating(request.getRating())
                .comments(request.getComments())
                .reopenRequested(request.getReopenRequested() != null ? request.getReopenRequested() : false)
                .reopenReason(request.getReopenReason())
                .build();

        feedbackRepository.save(feedback);

        return mapToResponse(feedback, "Feedback submitted successfully");
    }

    @Override
    public FeedbackResponse getFeedbackByGrievanceId(Long grievanceId) {
        Feedback feedback = feedbackRepository.findByGrievanceId(grievanceId)
                .orElseThrow(() -> new RuntimeException("Feedback not found for grievance: " + grievanceId));
        return mapToResponse(feedback, null);
    }

    @Override
    public List<FeedbackResponse> getFeedbackByCitizenId(Long citizenId) {
        List<Feedback> feedbacks = feedbackRepository.findByCitizenId(citizenId);
        return feedbacks.stream().map(f -> mapToResponse(f, null)).toList();
    }

    @Override
    public Double getAverageRating() {
        Double avg = feedbackRepository.findAverageRating();
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    private FeedbackResponse mapToResponse(Feedback feedback, String message) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .grievanceId(feedback.getGrievanceId())
                .citizenId(feedback.getCitizenId())
                .rating(feedback.getRating())
                .comments(feedback.getComments())
                .reopenRequested(feedback.getReopenRequested())
                .reopenReason(feedback.getReopenReason())
                .createdAt(feedback.getCreatedAt())
                .message(message)
                .build();
    }
}