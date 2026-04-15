package com.ft.back.budget.infrastructure.persistence;

import com.ft.back.budget.application.port.BudgetAlertRepository;
import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.domain.BudgetAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BudgetAlertRepositoryImpl implements BudgetAlertRepository {

    private final JpaBudgetAlertRepository jpaBudgetAlertRepository;

    @Override
    public BudgetAlert save(BudgetAlert alert) {
        return jpaBudgetAlertRepository.save(alert);
    }

    @Override
    public List<AlertType> findSentAlertTypesByBudgetId(Long budgetId) {
        return jpaBudgetAlertRepository.findAlertTypesByBudgetId(budgetId);
    }
}
