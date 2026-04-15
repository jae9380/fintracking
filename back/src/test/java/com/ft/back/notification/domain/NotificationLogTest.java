package com.ft.back.notification.domain;

import com.ft.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ft.back.common.exception.ErrorCode.NOTIFICATION_NO_ACCESS;
import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationLog 도메인 테스트")
class NotificationLogTest {

    private NotificationLog createLog(Long userId) {
        return NotificationLog.create(
                userId,
                NotificationType.BUDGET_WARNING,
                NotificationChannel.IN_APP,
                "예산 경고",
                "예산의 50%를 사용했습니다.",
                true
        );
    }

    @Nested
    @DisplayName("알림 생성")
    class Create {

        @Test
        @DisplayName("성공 - 알림이 정상 생성되며 초기 읽음 상태는 false다")
        void create_success_isReadFalse() {
            // when
            NotificationLog log = createLog(1L);

            // then
            assertThat(log.getUserId()).isEqualTo(1L);
            assertThat(log.getType()).isEqualTo(NotificationType.BUDGET_WARNING);
            assertThat(log.getChannel()).isEqualTo(NotificationChannel.IN_APP);
            assertThat(log.getTitle()).isEqualTo("예산 경고");
            assertThat(log.isRead()).isFalse();
            assertThat(log.isSuccess()).isTrue();
            assertThat(log.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 발송 실패 알림도 저장된다")
        void create_success_withFailure() {
            // when
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.BUDGET_EXCEEDED, NotificationChannel.FCM,
                    "예산 초과", "예산을 초과했습니다.", false
            );

            // then
            assertThat(log.isSuccess()).isFalse();
            assertThat(log.isRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("소유자 검증")
    class ValidateOwner {

        @Test
        @DisplayName("성공 - 본인이면 예외 없음")
        void validateOwner_success_sameUser() {
            // given
            NotificationLog log = createLog(1L);

            // when & then
            assertThatNoException().isThrownBy(() -> log.validateOwner(1L));
        }

        @Test
        @DisplayName("실패 - 다른 사용자면 NOTIFICATION_NO_ACCESS 예외 발생")
        void validateOwner_fail_differentUser() {
            // given
            NotificationLog log = createLog(1L);

            // when & then
            assertThatThrownBy(() -> log.validateOwner(2L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(NOTIFICATION_NO_ACCESS));
        }
    }

    @Nested
    @DisplayName("읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("성공 - 읽음 처리 후 isRead가 true가 된다")
        void markAsRead_success() {
            // given
            NotificationLog log = createLog(1L);
            assertThat(log.isRead()).isFalse();

            // when
            log.markAsRead();

            // then
            assertThat(log.isRead()).isTrue();
        }

        @Test
        @DisplayName("성공 - 이미 읽음 상태에서 재호출해도 true 유지")
        void markAsRead_success_idempotent() {
            // given
            NotificationLog log = createLog(1L);
            log.markAsRead();

            // when
            log.markAsRead();

            // then
            assertThat(log.isRead()).isTrue();
        }
    }
}
