package com.grievance.feedback_service.service;

import com.grievance.feedback_service.dto.CreateFeedbackRequest;
import com.grievance.feedback_service.dto.FeedbackResponse;
import com.grievance.feedback_service.entity.Feedback;
import com.grievance.feedback_service.repository.FeedbackRepository;
import com.grievance.feedback_service.service.impl.FeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private Feedback testFeedback;
    private CreateFeedbackRequest createRequest;

    @BeforeEach
    void setUp() {
        testFeedback = Feedback.builder()
                .id(1L)
                .grievanceId(1L)
                .citizenId(1L)
                .rating(5)
                .comments("Excellent service")
                .reopenRequested(false)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new CreateFeedbackRequest();
        createRequest.setGrievanceId(1L);
        createRequest.setRating(5);
        createRequest.setComments("Excellent service");
        createRequest.setReopenRequested(false);
    }

    @Test
    void submitFeedback_Success() {
        when(feedbackRepository.existsByGrievanceId(1L)).thenReturn(false);
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(testFeedback);

        FeedbackResponse response = feedbackService.submitFeedback(createRequest, 1L);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    void submitFeedback_DuplicateFeedback_ThrowsException() {
        when(feedbackRepository.existsByGrievanceId(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> feedbackService.submitFeedback(createRequest, 1L));
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void getFeedbackByGrievanceId_Success() {
        when(feedbackRepository.findByGrievanceId(1L)).thenReturn(Optional.of(testFeedback));

        FeedbackResponse response = feedbackService.getFeedbackByGrievanceId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getGrievanceId());
    }

    @Test
    void getFeedbackByCitizenId_Success() {
        when(feedbackRepository.findByCitizenId(1L)).thenReturn(Arrays.asList(testFeedback));

        List<FeedbackResponse> responses = feedbackService.getFeedbackByCitizenId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getAverageRating_Success() {
        when(feedbackRepository.findAverageRating()).thenReturn(4.5);

        Double avgRating = feedbackService.getAverageRating();

        assertNotNull(avgRating);
        assertEquals(4.5, avgRating);
    }
}
