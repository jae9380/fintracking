package com.ft.back.notification.application.port;

import com.ft.back.notification.domain.NotificationSettings;

import java.util.Optional;

public interface NotificationSettingsRepository {
    NotificationSettings save(NotificationSettings settings);
    Optional<NotificationSettings> findByUserId(Long userId);
}
