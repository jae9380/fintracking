package com.ft.back.auth.application.dto;

public record LoginResult(
        String accessToken,
        String refreshToken
) {}
