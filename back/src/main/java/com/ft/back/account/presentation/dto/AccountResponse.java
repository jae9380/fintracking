package com.ft.back.account.presentation.dto;

import com.ft.back.account.application.dto.AccountResult;
import com.ft.back.account.domain.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountName,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance
) {
    public static AccountResponse from(AccountResult result) {
        return new AccountResponse(
                result.id(),
                result.accountName(),
                result.maskedAccountNumber(),
                result.accountType(),
                result.balance()
        );
    }
}
