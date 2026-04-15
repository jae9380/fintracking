package com.ft.back.transaction.presentation;

import com.ft.back.common.response.ApiResponse;
import com.ft.back.transaction.application.TransactionService;
import com.ft.back.transaction.presentation.dto.CreateTransactionRequest;
import com.ft.back.transaction.presentation.dto.TransactionResponse;
import com.ft.back.transaction.presentation.dto.UpdateTransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transaction", description = "거래 내역 API")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "거래 생성")
    @PostMapping
    public ApiResponse<TransactionResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ApiResponse.created(TransactionResponse.from(transactionService.create(userId, request.toCommand())));
    }

    @Operation(summary = "거래 목록 조회")
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

    @Operation(summary = "거래 상세 조회")
    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transactionId) {
        return ApiResponse.success(TransactionResponse.from(transactionService.findById(userId, transactionId)));
    }

    @Operation(summary = "거래 수정 (금액/메모/날짜)")
    @PatchMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transactionId,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ApiResponse.success(
                TransactionResponse.from(transactionService.update(userId, transactionId, request.toCommand())));
    }

    @Operation(summary = "거래 삭제 (계좌 잔액 복구 포함)")
    @DeleteMapping("/{transactionId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transactionId) {
        transactionService.delete(userId, transactionId);
        return ApiResponse.noContent();
    }
}
