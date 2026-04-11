package com.ft.back.transaction.presentation;

import com.ft.back.common.response.ApiResponse;
import com.ft.back.transaction.application.TransactionService;
import com.ft.back.transaction.presentation.dto.CreateTransactionRequest;
import com.ft.back.transaction.presentation.dto.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ApiResponse<TransactionResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ApiResponse.created(TransactionResponse.from(transactionService.create(userId, request.toCommand())));
    }

    @GetMapping
    public ApiResponse<List<TransactionResponse>> findAll(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long accountId) {
        List<TransactionResponse> responses = transactionService.findAll(userId, accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transactionId) {
        return ApiResponse.success(TransactionResponse.from(transactionService.findById(userId, transactionId)));
    }

    @DeleteMapping("/{transactionId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transactionId) {
        transactionService.delete(userId, transactionId);
        return ApiResponse.noContent();
    }
}
