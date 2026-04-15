package com.ft.back.auth.domain;

/**
 * OAuth2 로그인 제공자 구분
 * EMAIL — 자체 이메일/비밀번호 로그인
 * KAKAO  — 카카오 소셜 로그인
 */
public enum OAuth2Provider {
    EMAIL,
    KAKAO
}
