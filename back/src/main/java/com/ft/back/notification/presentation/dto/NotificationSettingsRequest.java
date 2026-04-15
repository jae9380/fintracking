package com.ft.back.notification.presentation.dto;

public record NotificationSettingsRequest(
        boolean fcmEnabled,
        boolean emailEnabled
) {
}
