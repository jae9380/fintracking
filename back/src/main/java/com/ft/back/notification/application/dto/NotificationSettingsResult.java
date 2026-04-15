package com.ft.back.notification.application.dto;

public record NotificationSettingsResult(
        Long userId,
        boolean fcmEnabled,
        boolean emailEnabled
) {
    public static NotificationSettingsResult of(Long userId, boolean fcmEnabled, boolean emailEnabled) {
        return new NotificationSettingsResult(userId, fcmEnabled, emailEnabled);
    }
}
