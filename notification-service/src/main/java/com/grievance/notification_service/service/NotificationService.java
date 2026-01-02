package com.grievance.notification_service.service;

import com.grievance.notification_service.dto.CreateNotificationRequest;
import com.grievance.notification_service.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    List<NotificationResponse> getNotificationsByUserId(Long userId);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    Long getUnreadCount(Long userId);

    NotificationResponse markAsRead(Long notificationId);

    void markAllAsRead(Long userId);
}