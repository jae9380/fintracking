package com.ft.back.transaction.application.event;

import com.ft.back.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreatedEvent(
        Long userId,
        Long categoryId,
        TransactionType type,
        BigDecimal amount,
        LocalDate transactionDate
) {}
