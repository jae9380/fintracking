package com.ft.back.budget.application.dto;

import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.domain.Budget;

import java.math.BigDecimal;
import java.util.List;

public record BudgetResult(
        Long id,
        Long categoryId,
        String yearMonth,
        BigDecimal amount,
        BigDecimal spent,
        BigDecimal usageRate,
        List<AlertType> sentAlerts
) {
    public static BudgetResult of(Budget budget, BigDecimal spent, List<AlertType> sentAlerts) {
        return new BudgetResult(
                budget.getId(),
                budget.getCategoryId(),
                budget.getYearMonth(),
                budget.getAmount(),
                spent,
                budget.calculateUsageRate(spent),
                sentAlerts
        );
    }
}
