package com.ft.back.transaction.presentation.dto;

import com.ft.back.transaction.application.dto.TransactionResult;
import com.ft.back.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long accountId,
        Long categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(TransactionResult result) {
        return new TransactionResponse(
                result.id(),
                result.accountId(),
                result.categoryId(),
                result.type(),
                result.amount(),
                result.description(),
                result.transactionDate(),
                result.createdAt()
        );
    }
}
