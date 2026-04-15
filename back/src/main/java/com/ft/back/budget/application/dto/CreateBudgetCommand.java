package com.ft.back.budget.application.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CreateBudgetCommand(Long categoryId, YearMonth yearMonth, BigDecimal amount) {}
