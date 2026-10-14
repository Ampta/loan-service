package com.ampta.dto.response;

import com.ampta.entity.enums.NotificationType;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private Long referenceId;
    private Instant createdAt;
    private Instant readAt;
}