package com.ft.back.notification.infrastructure.persistence;

import com.ft.back.notification.domain.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaNotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    Optional<NotificationSettings> findByUserId(Long userId);
}
