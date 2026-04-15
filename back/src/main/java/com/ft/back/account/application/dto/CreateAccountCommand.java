package com.ft.back.account.application.dto;

import com.ft.back.account.domain.AccountType;

public record CreateAccountCommand(
        String accountName,
        String accountNumber,
        AccountType accountType
) {}
