package com.ft.back.auth.application;

import com.ft.back.auth.application.dto.LoginCommand;
import com.ft.back.auth.application.dto.SignupCommand;
import com.ft.back.auth.application.handler.EmailAuthHandler;
import com.ft.back.auth.application.handler.KakaoAuthHandler;
import com.ft.back.auth.application.port.RefreshTokenRepository;
import com.ft.back.auth.application.port.TokenProvider;
import com.ft.back.auth.application.port.UserRepository;
import com.ft.back.auth.domain.RefreshToken;
import com.ft.back.auth.domain.User;
import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenProvider tokenProvider;
    @Mock private EmailAuthHandler emailAuthHandler;
    @Mock private KakaoAuthHandler kakaoAuthHandler;
    @Mock private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("성공 - 신규 이메일이면 저장 후 userId를 반환한다")
        void signup_whenNewEmail_returnsUserId() {
            // given
            SignupCommand command = new SignupCommand("test@example.com", "password123!");
            User savedUser = User.create("test@example.com", "encodedPw");
            given(userRepository.existsByEmail(command.email())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPw");
            given(userRepository.save(any())).willReturn(savedUser);

            // when
            authService.signup(command);

            // then
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("실패 - 이메일 중복이면 예외 발생")
        void signup_whenDuplicateEmail_throwsCustomException() {
            // given
            SignupCommand command = new SignupCommand("test@example.com", "password123!");
            given(userRepository.existsByEmail(command.email())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_EMAIL_EXISTS));
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        @Test
        @DisplayName("성공 - 유효한 토큰이면 새 AccessToken을 반환한다")
        void reissue_whenValidToken_returnsNewAccessToken() {
            // given
            String rawToken = "valid-refresh-token";
            Long userId = 1L;
            RefreshToken refreshToken = RefreshToken.create(userId, rawToken, LocalDateTime.now().plusDays(7));
            given(refreshTokenRepository.findByToken(rawToken)).willReturn(Optional.of(refreshToken));
            given(tokenProvider.createAccessToken(userId)).willReturn("new-access-token");
            given(tokenProvider.createRefreshToken(userId)).willReturn("new-refresh-token");
            given(tokenProvider.getRefreshTokenExpiry()).willReturn(LocalDateTime.now().plusDays(7));
            given(refreshTokenRepository.save(any())).willReturn(refreshToken);

            // when
            String newAccessToken = authService.reissue(rawToken);

            // then
            assertThat(newAccessToken).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("실패 - 토큰이 존재하지 않으면 예외 발생")
        void reissue_whenTokenNotFound_throwsCustomException() {
            // given
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue("invalid-token"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공 - RefreshToken 삭제를 호출한다")
        void logout_whenCalled_deletesRefreshToken() {
            // given
            Long userId = 1L;

            // when
            authService.logout(userId);

            // then
            then(refreshTokenRepository).should().deleteByUserId(userId);
        }
    }
}
