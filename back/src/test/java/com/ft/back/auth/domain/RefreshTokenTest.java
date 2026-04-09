package com.ft.back.auth.domain;

import com.ft.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.ft.back.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshToken 도메인 테스트")
class RefreshTokenTest {

    @Nested
    @DisplayName("토큰 생성")
    class Create {

        @Test
        @DisplayName("성공 - 토큰이 정상 생성된다")
        void success_createToken() {
            // given
            Long userId = 1L;
            String token = "valid-refresh-token";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

            // when
            RefreshToken refreshToken = RefreshToken.create(userId, token, expiresAt);

            // then
            assertThat(refreshToken.getToken()).isEqualTo("valid-refresh-token");
            assertThat(refreshToken.isExpired()).isFalse();
        }

        @Test
        @DisplayName("실패 - 만료 시간이 과거면 예외 발생")
        void fail_pastExpiresAt_throwsException() {
            // given
            Long userId = 1L;
            String token = "valid-refresh-token";
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

            // when & then
            assertThatThrownBy(() -> RefreshToken.create(userId, token, pastTime))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - 토큰이 공백이면 예외 발생")
        void fail_blankToken_throwsException() {
            // given
            Long userId = 1L;
            String blankToken = " ";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

            // when & then
            assertThatThrownBy(() -> RefreshToken.create(userId, blankToken, expiresAt))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_REQUIRED));
        }
    }

    @Nested
    @DisplayName("토큰 유효성 검증")
    class ValidateToken {

        @Test
        @DisplayName("성공 - 유효한 토큰이면 예외 없음")
        void success_validToken_noException() {
            // given
            String token = "valid-token";
            RefreshToken refreshToken = RefreshToken.create(1L, token, LocalDateTime.now().plusDays(7));

            // when & then
            assertThatNoException().isThrownBy(() -> refreshToken.validateToken(token));
        }

        @Test
        @DisplayName("실패 - 토큰 값이 다르면 예외 발생")
        void fail_differentToken_throwsException() {
            // given
            RefreshToken refreshToken = RefreshToken.create(1L, "original-token", LocalDateTime.now().plusDays(7));
            String wrongToken = "wrong-token";

            // when & then
            assertThatThrownBy(() -> refreshToken.validateToken(wrongToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_INVALID));
        }
    }

    @Nested
    @DisplayName("토큰 갱신 (Rotate)")
    class Rotate {

        @Test
        @DisplayName("성공 - 토큰이 새 값으로 교체된다")
        void success_tokenRotated() {
            // given
            RefreshToken refreshToken = RefreshToken.create(1L, "old-token", LocalDateTime.now().plusDays(7));
            String newToken = "new-token";
            LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(7);

            // when
            refreshToken.rotate(newToken, newExpiresAt);

            // then
            assertThat(refreshToken.getToken()).isEqualTo("new-token");
        }

        @Test
        @DisplayName("실패 - 새 토큰이 공백이면 예외 발생")
        void fail_blankNewToken_throwsException() {
            // given
            RefreshToken refreshToken = RefreshToken.create(1L, "old-token", LocalDateTime.now().plusDays(7));
            String blankToken = "";
            LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(7);

            // when & then
            assertThatThrownBy(() -> refreshToken.rotate(blankToken, newExpiresAt))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_NEW_REFRESH_TOKEN_REQUIRED));
        }
    }
}
