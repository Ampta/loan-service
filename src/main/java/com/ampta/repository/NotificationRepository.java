package com.ampta.repository;

import com.ampta.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Notification> findByNotificationIdAndUserUserId(
            Long notificationId,
            Long userId
    );

    long countByUserUserIdAndReadAtIsNull(
            Long userId
    );
}