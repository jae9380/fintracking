package com.ft.back.auth.domain;

import com.ft.back.common.entity.BaseEntity;
import com.ft.back.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ft.back.common.exception.ErrorCode.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt 해시

    private User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static User create(String email, String encodedPassword) {
        validateEmail(email);
        return new User(email, encodedPassword);
    }

    // 비밀번호 검증 — PasswordValidator로 Spring 의존성 분리
    public void validatePassword(String rawPassword, PasswordValidator validator) {
        if (!validator.matches(rawPassword, this.password)) {
            throw new CustomException(AUTH_INVALID_PASSWORD);
        }
    }

    // 비밀번호 변경
    public void changePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.isBlank()) {
            throw new CustomException(AUTH_PASSWORD_REQUIRED);
        }
        this.password = newEncodedPassword;
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException(AUTH_EMAIL_REQUIRED);
        }
        if (!email.contains("@")) {
            throw new CustomException(AUTH_EMAIL_INVALID_FORMAT);
        }
    }
}
