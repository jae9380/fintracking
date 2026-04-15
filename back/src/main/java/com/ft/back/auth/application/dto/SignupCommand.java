package com.ft.back.auth.application.dto;

public record SignupCommand(
        String email,
        String rawPassword
) {}
