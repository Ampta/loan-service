package com.ampta.service;

import com.ampta.dto.response.NotificationResponse;
import com.ampta.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    void sendNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Long referenceId
    );

    List<NotificationResponse> getUserNotifications(
            Long userId
    );

    void markAsRead(
            Long userId,
            Long notificationId
    );

    long getUnreadCount(
            Long userId
    );
}