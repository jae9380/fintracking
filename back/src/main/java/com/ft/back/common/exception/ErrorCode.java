package com.ft.back.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    AUTH_EMAIL_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    AUTH_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    AUTH_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 비어있을 수 없습니다."),
    AUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "이메일은 비어있을 수 없습니다."),
    AUTH_EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "올바른 이메일 형식이 아닙니다."),
    AUTH_REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "토큰은 비어있을 수 없습니다."),
    AUTH_NEW_REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "새 토큰은 비어있을 수 없습니다."),
    AUTH_INVALID_EXPIRATION(HttpStatus.BAD_REQUEST, "만료 시간은 현재 시간 이후여야 합니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_OAUTH2_FAILED(HttpStatus.BAD_REQUEST, "Kakao OAuth2 인증에 실패했습니다."),
    AUTH_OAUTH2_EMAIL_MISSING(HttpStatus.BAD_REQUEST, "Kakao 계정에 이메일 정보가 없습니다. 카카오 계정 설정에서 이메일 제공에 동의해 주세요."),

    // Account
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다."),
    ACCOUNT_NO_ACCESS(HttpStatus.FORBIDDEN, "해당 계좌에 대한 접근 권한이 없습니다."),
    ACCOUNT_INVALID_TYPE(HttpStatus.BAD_REQUEST, "잘못된 계좌 유형입니다."),
    ACCOUNT_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    ACCOUNT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다."),
    ACCOUNT_INVALID_NAME(HttpStatus.BAD_REQUEST, "계좌명은 비어있을 수 없습니다."),
    ACCOUNT_INVALID_NUMBER(HttpStatus.BAD_REQUEST, "계좌번호는 비어있을 수 없습니다."),
    ACCOUNT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "계좌 소유자가 아닙니다."),

    // Transaction
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "거래 내역을 찾을 수 없습니다."),
    TRANSACTION_NO_ACCESS(HttpStatus.FORBIDDEN, "해당 거래에 대한 접근 권한이 없습니다."),
    TRANSACTION_INVALID_TYPE(HttpStatus.BAD_REQUEST, "잘못된 거래 유형입니다."),
    TRANSACTION_INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "카테고리명은 비어있을 수 없습니다."),
    TRANSACTION_INVALID_DATE(HttpStatus.BAD_REQUEST,"거래일은 비어있을 수 없습니다."),
    TRANSACTION_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "수입/지출 거래는 카테고리가 필요합니다."),
    TRANSACTION_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "거래 금액은 0보다 커야 합니다."),

    // Budget
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "예산 정보를 찾을 수 없습니다."),
    BUDGET_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 예산입니다."),
    BUDGET_NO_ACCESS(HttpStatus.FORBIDDEN, "해당 예산에 대한 접근 권한이 없습니다."),
    BUDGET_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "예산 금액은 0보다 커야 합니다."),
    BUDGET_EXPENSE_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "지출 금액은 0 이상이어야 합니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    NOTIFICATION_NO_ACCESS(HttpStatus.FORBIDDEN, "해당 알림에 대한 접근 권한이 없습니다."),
    NOTIFICATION_INVALID_WEBHOOK(HttpStatus.BAD_REQUEST, "유효하지 않은 Slack 웹훅 URL입니다.");

    private final HttpStatus status;
    private final String message;
}
