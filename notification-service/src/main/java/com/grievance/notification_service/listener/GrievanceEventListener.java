package com.grievance.notification_service.listener;

import com.grievance.notification_service.config.RabbitMQConfig;
import com.grievance.notification_service.dto.CreateNotificationRequest;
import com.grievance.notification_service.dto.GrievanceEvent;
import com.grievance.notification_service.enums.NotificationType;
import com.grievance.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrievanceEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GRIEVANCE_CREATED)
    public void handleGrievanceCreated(GrievanceEvent event) {
        log.info("Received grievance created event: {}", event.getGrievanceNumber());

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(event.getCitizenId())
                .grievanceId(event.getGrievanceId())
                .grievanceNumber(event.getGrievanceNumber())
                .type(NotificationType.GRIEVANCE_SUBMITTED)
                .title("Grievance Submitted Successfully")
                .message(buildGrievanceCreatedMessage(event))
                .userEmail(event.getCitizenEmail())
                .build();

        notificationService.createNotification(request);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STATUS_CHANGED)
    public void handleStatusChanged(GrievanceEvent event) {
        log.info("Received status changed event: {} -> {}", event.getOldStatus(), event.getNewStatus());

        NotificationType type = mapStatusToNotificationType(event.getNewStatus());

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(event.getCitizenId())
                .grievanceId(event.getGrievanceId())
                .grievanceNumber(event.getGrievanceNumber())
                .type(type)
                .title(buildStatusChangeTitle(event.getNewStatus()))
                .message(buildStatusChangeMessage(event))
                .userEmail(event.getCitizenEmail())
                .build();

        notificationService.createNotification(request);
    }

    private String buildGrievanceCreatedMessage(GrievanceEvent event) {
        return String.format(
                "Your grievance '%s' has been submitted successfully. " +
                "Track it using grievance number: %s. " +
                "Department: %s, Category: %s",
                event.getTitle(),
                event.getGrievanceNumber(),
                event.getDepartment(),
                event.getCategory()
        );
    }

    private String buildStatusChangeMessage(GrievanceEvent event) {
        String message = String.format(
                "Your grievance '%s' (ID: %s) status has been updated from %s to %s.",
                event.getTitle(),
                event.getGrievanceNumber(),
                event.getOldStatus() != null ? event.getOldStatus() : "NEW",
                event.getNewStatus()
        );

        if (event.getRemarks() != null && !event.getRemarks().isEmpty()) {
            message += " Remarks: " + event.getRemarks();
        }

        return message;
    }

    private String buildStatusChangeTitle(String newStatus) {
        return switch (newStatus) {
            case "ASSIGNED" -> "Officer Assigned to Your Grievance";
            case "IN_PROGRESS" -> "Grievance In Progress";
            case "RESOLVED" -> "Grievance Resolved";
            case "CLOSED" -> "Grievance Closed";
            case "REJECTED" -> "Grievance Rejected";
            default -> "Grievance Status Updated";
        };
    }

    private NotificationType mapStatusToNotificationType(String status) {
        return switch (status) {
            case "ASSIGNED" -> NotificationType.GRIEVANCE_ASSIGNED;
            case "RESOLVED" -> NotificationType.GRIEVANCE_RESOLVED;
            case "CLOSED" -> NotificationType.GRIEVANCE_CLOSED;
            default -> NotificationType.STATUS_CHANGED;
        };
    }
}