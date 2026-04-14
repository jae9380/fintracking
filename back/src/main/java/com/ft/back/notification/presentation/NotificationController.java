package com.ft.back.notification.presentation;

import com.ft.back.common.response.ApiResponse;
import com.ft.back.notification.application.NotificationService;
import com.ft.back.notification.presentation.dto.NotificationResponse;
import com.ft.back.notification.presentation.dto.NotificationSettingsRequest;
import com.ft.back.notification.presentation.dto.NotificationSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> findAll(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<NotificationResponse> responses = notificationService.findAll(userId, read, pageable)
                .map(NotificationResponse::from);
        return ApiResponse.success(responses);
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        return ApiResponse.success(
                NotificationResponse.from(notificationService.markAsRead(userId, notificationId)));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.noContent();
    }

    @PostMapping("/settings")
    public ApiResponse<NotificationSettingsResponse> updateSettings(
            @AuthenticationPrincipal Long userId,
            @RequestBody NotificationSettingsRequest request
    ) {
        NotificationSettingsResponse response = NotificationSettingsResponse.from(
                notificationService.updateSettings(userId, request.fcmEnabled(), request.emailEnabled())
        );
        return ApiResponse.success(response);
    }
}
