package com.ft.back.batch.application.decorator;

import com.ft.back.auth.infrastructure.persistence.JpaUserRepository;
import com.ft.back.batch.application.BatchJobExecutor;
import com.ft.back.notification.application.NotificationService;
import com.ft.back.notification.domain.NotificationType;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class NotificationBatchDecorator extends AbstractBatchDecorator {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월");

    private final JpaUserRepository jpaUserRepository;
    private final NotificationService notificationService;

    public NotificationBatchDecorator(BatchJobExecutor delegate,
                                      JpaUserRepository jpaUserRepository,
                                      NotificationService notificationService) {
        super(delegate);
        this.jpaUserRepository = jpaUserRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void execute(YearMonth yearMonth) {
        delegate.execute(yearMonth);

        String displayMonth = yearMonth.format(DISPLAY_FORMATTER);
        String title = displayMonth + " 월간 리포트";
        String message = displayMonth + " 지출/수입 통계가 집계되었습니다. 앱에서 확인해 보세요.";

        List<Long> userIds = jpaUserRepository.findAll()
                .stream()
                .map(user -> user.getId())
                .toList();

        for (Long userId : userIds) {
            try {
                notificationService.send(userId, NotificationType.MONTHLY_REPORT, title, message);
            } catch (Exception e) {
                log.error("[Batch][Notification] 알림 발송 실패 — userId={}, error={}", userId, e.getMessage());
            }
        }

        log.info("[Batch][Notification] 월간 리포트 알림 발송 완료 — yearMonth={}, userCount={}",
                yearMonth, userIds.size());
    }
}
