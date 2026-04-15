package com.ft.back.auth.domain;

import com.ft.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ft.back.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("User 도메인 테스트")
class UserTest {

    // 테스트용 PasswordValidator (Spring 의존 없이 단순 비교)
    private final PasswordValidator validator = String::equals;

    @Nested
    @DisplayName("유저 생성")
    class Create {

        @Test
        @DisplayName("성공 - 유저가 정상 생성된다")
        void success_createUser() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encodedPassword123";

            // when
            User user = User.create(email, encodedPassword);

            // then
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isEqualTo("encodedPassword123");
        }

        @Test
        @DisplayName("실패 - 이메일이 공백이면 예외 발생")
        void fail_blankEmail_throwsException() {
            // given
            String blankEmail = " ";
            String encodedPassword = "encodedPassword123";

            // when & then
            assertThatThrownBy(() -> User.create(blankEmail, encodedPassword))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_EMAIL_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 이메일 형식이 잘못되면 예외 발생")
        void fail_invalidEmailFormat_throwsException() {
            // given
            String invalidEmail = "not-an-email";
            String encodedPassword = "encodedPassword123";

            // when & then
            assertThatThrownBy(() -> User.create(invalidEmail, encodedPassword))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_EMAIL_INVALID_FORMAT));
        }
    }

    @Nested
    @DisplayName("비밀번호 검증")
    class ValidatePassword {

        @Test
        @DisplayName("성공 - 비밀번호가 일치하면 예외 없음")
        void success_correctPassword_noException() {
            // given
            User user = User.create("test@example.com", "myPassword");
            String rawPassword = "myPassword";

            // when & then
            assertThatNoException().isThrownBy(() -> user.validatePassword(rawPassword, validator));
        }

        @Test
        @DisplayName("실패 - 비밀번호가 틀리면 예외 발생")
        void fail_wrongPassword_throwsException() {
            // given
            User user = User.create("test@example.com", "myPassword");
            String wrongPassword = "wrongPassword";

            // when & then
            assertThatThrownBy(() -> user.validatePassword(wrongPassword, validator))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_INVALID_PASSWORD));
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공 - 비밀번호가 변경된다")
        void success_passwordChanged() {
            // given
            User user = User.create("test@example.com", "oldPassword");
            String newEncodedPassword = "newEncodedPassword";

            // when
            user.changePassword(newEncodedPassword);

            // then
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 공백이면 예외 발생")
        void fail_blankNewPassword_throwsException() {
            // given
            User user = User.create("test@example.com", "oldPassword");
            String blankPassword = "";

            // when & then
            assertThatThrownBy(() -> user.changePassword(blankPassword))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_PASSWORD_REQUIRED));
        }
    }
}
