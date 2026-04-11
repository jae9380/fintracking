package com.ft.back.auth.application.dto;

public record LoginCommand(
        String email,
        String rawPassword
) {}
