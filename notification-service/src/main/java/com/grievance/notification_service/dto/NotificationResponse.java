package com.grievance.notification_service.dto;

import com.grievance.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private Long grievanceId;
    private String grievanceNumber;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private Boolean emailSent;
    private LocalDateTime createdAt;
}