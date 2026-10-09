package com.ampta.service.impl;

import com.ampta.dto.response.NotificationResponse;
import com.ampta.entity.Notification;
import com.ampta.entity.enums.NotificationType;
import com.ampta.entity.User;
import com.ampta.repository.NotificationRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public void sendNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Long referenceId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .readAt(null)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(
            Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notification ->
                        modelMapper.map(
                                notification,
                                NotificationResponse.class
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(
            Long userId,
            Long notificationId) {

        Notification notification =
                notificationRepository
                        .findByNotificationIdAndUserUserId(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                ));

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserUserIdAndReadAtIsNull(userId);
    }
}