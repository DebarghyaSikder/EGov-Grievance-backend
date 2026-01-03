package com.grievance.feedback_service.exception;

public class DuplicateFeedbackException extends RuntimeException {
    public DuplicateFeedbackException(String message) {
        super(message);
    }

    public DuplicateFeedbackException(Long grievanceId) {
        super("Feedback already submitted for grievance ID: " + grievanceId);
    }
}