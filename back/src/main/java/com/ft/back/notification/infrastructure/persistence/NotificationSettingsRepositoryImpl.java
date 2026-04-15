package com.ft.back.notification.infrastructure.persistence;

import com.ft.back.notification.application.port.NotificationSettingsRepository;
import com.ft.back.notification.domain.NotificationSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationSettingsRepositoryImpl implements NotificationSettingsRepository {

    private final JpaNotificationSettingsRepository jpaNotificationSettingsRepository;

    @Override
    public NotificationSettings save(NotificationSettings settings) {
        return jpaNotificationSettingsRepository.save(settings);
    }

    @Override
    public Optional<NotificationSettings> findByUserId(Long userId) {
        return jpaNotificationSettingsRepository.findByUserId(userId);
    }
}
