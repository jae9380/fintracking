package com.ft.back.budget.application.port;

import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.domain.BudgetAlert;

import java.util.List;

public interface BudgetAlertRepository {
    BudgetAlert save(BudgetAlert alert);
    List<AlertType> findSentAlertTypesByBudgetId(Long budgetId);
}
