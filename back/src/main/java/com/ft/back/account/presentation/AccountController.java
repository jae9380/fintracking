package com.ft.back.account.presentation;

import com.ft.back.account.application.AccountService;
import com.ft.back.account.application.dto.AccountResult;
import com.ft.back.account.presentation.dto.AccountResponse;
import com.ft.back.account.presentation.dto.CreateAccountRequest;
import com.ft.back.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ApiResponse<AccountResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResult result = accountService.create(userId, request.toCommand());
        return ApiResponse.created(AccountResponse.from(result));
    }

    @GetMapping
    public ApiResponse<List<AccountResponse>> findAll(
            @AuthenticationPrincipal Long userId) {
        List<AccountResponse> responses = accountService.findAll(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{accountId}")
    public ApiResponse<AccountResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long accountId) {
        AccountResult result = accountService.findById(userId, accountId);
        return ApiResponse.success(AccountResponse.from(result));
    }

    @DeleteMapping("/{accountId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long accountId) {
        accountService.delete(userId, accountId);
        return ApiResponse.noContent();
    }
}
