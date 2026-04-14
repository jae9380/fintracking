package com.ft.back.notification.application;

import com.ft.back.common.exception.CustomException;
import com.ft.back.notification.application.dto.NotificationResult;
import com.ft.back.notification.application.dto.NotificationSettingsResult;
import com.ft.back.notification.application.port.NotificationRepository;
import com.ft.back.notification.domain.NotificationChannel;
import com.ft.back.notification.domain.NotificationLog;
import com.ft.back.notification.domain.NotificationType;
import com.ft.back.notification.domain.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ft.back.common.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final Map<NotificationChannel, NotificationSender> senderMap;

    public NotificationService(
            NotificationRepository notificationRepository,
            List<NotificationSender> senders
    ) {
        this.notificationRepository = notificationRepository;
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(NotificationSender::channel, Function.identity()));
    }

    // 알림 목록 조회 (페이지네이션, 읽음 여부 필터)
    @Transactional(readOnly = true)
    public Page<NotificationResult> findAll(Long userId, Boolean isRead, Pageable pageable) {
        return notificationRepository.findAllByUserId(userId, isRead, pageable)
                .map(NotificationResult::from);
    }

    // 단건 읽음 처리
    @Transactional
    public NotificationResult markAsRead(Long userId, Long notificationId) {
        NotificationLog log = getNotificationLog(notificationId);
        log.validateOwner(userId);
        log.markAsRead();
        return NotificationResult.from(log);
    }

    // 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void send(Long userId, NotificationType type, String title, String message) {
        senderMap.forEach((channel, sender) -> {
            boolean success = false;
            try {
                success = sender.send(userId, type, title, message);
            } catch (Exception e) {
                log.error("[Notification] 발송 실패 — channel={}, userId={}, error={}",
                        channel, userId, e.getMessage());
            }

            NotificationLog notificationLog = NotificationLog.create(
                    userId, type, channel, title, message, success);
            notificationRepository.save(notificationLog);

            log.info("[Notification] 발송 완료 — channel={}, userId={}, type={}, success={}",
                    channel, userId, type, success);
        });
    }

    /**
     * 채널 설정 처리.
     * 현재는 설정값을 응답으로 반환한다.
     * 실제 서비스에서는 user_notification_settings 테이블에 영속화한다.
     */
    public NotificationSettingsResult updateSettings(Long userId, boolean fcmEnabled, boolean emailEnabled) {
        log.info("[Notification] 설정 변경 — userId={}, fcm={}, email={}", userId, fcmEnabled, emailEnabled);
        return NotificationSettingsResult.of(userId, fcmEnabled, emailEnabled);
    }

    private NotificationLog getNotificationLog(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));
    }
}
