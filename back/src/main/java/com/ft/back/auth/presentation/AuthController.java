package com.ft.back.auth.presentation;

import com.ft.back.auth.application.AuthService;
import com.ft.back.auth.application.dto.LoginResult;
import com.ft.back.auth.presentation.dto.AuthResponse;
import com.ft.back.auth.presentation.dto.LoginRequest;
import com.ft.back.auth.presentation.dto.SignupRequest;
import com.ft.back.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Long> signup(@Valid @RequestBody SignupRequest request) {
        Long userId = authService.signup(request.toCommand());
        return ApiResponse.created(userId);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.toCommand());
        return ApiResponse.success(AuthResponse.from(result));
    }

    @PostMapping("/reissue")
    public ApiResponse<String> reissue(
            @RequestHeader("Authorization") String bearerToken) {
        String refreshToken = bearerToken.replace("Bearer ", "");
        String newAccessToken = authService.reissue(refreshToken);
        return ApiResponse.success(newAccessToken);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ApiResponse.noContent();
    }
}
