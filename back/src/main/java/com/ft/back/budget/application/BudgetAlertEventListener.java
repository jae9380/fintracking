package com.ft.back.budget.application;

import com.ft.back.budget.application.port.BudgetAlertRepository;
import com.ft.back.budget.application.port.BudgetRepository;
import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.domain.Budget;
import com.ft.back.budget.domain.BudgetAlert;
import com.ft.back.budget.domain.handler.BudgetAlertHandler;
import com.ft.back.budget.domain.handler.Exceeded100Handler;
import com.ft.back.budget.domain.handler.Warning50Handler;
import com.ft.back.budget.domain.handler.Warning80Handler;
import com.ft.back.transaction.application.event.TransactionCreatedEvent;
import com.ft.back.transaction.application.port.TransactionRepository;
import com.ft.back.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


    // note: Phase 3 MSA 분리 시 Kafka Consumer로 교체 예정.

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetAlertEventListener {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final TransactionRepository transactionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        if (event.type() != TransactionType.EXPENSE || event.categoryId() == null) {
            return;
        }

        YearMonth yearMonth = YearMonth.from(event.transactionDate());

        budgetRepository.findByUserIdAndCategoryIdAndYearMonth(
                        event.userId(), event.categoryId(), yearMonth.toString())
                .ifPresent(budget -> checkAndLogAlerts(budget, yearMonth));
    }

    private void checkAndLogAlerts(Budget budget, YearMonth yearMonth) {
        BigDecimal spent = transactionRepository.sumExpenseByUserIdAndCategoryIdAndYearMonth(
                budget.getUserId(), budget.getCategoryId(),
                yearMonth.getYear(), yearMonth.getMonthValue());

        List<AlertType> sentAlerts = budgetAlertRepository.findSentAlertTypesByBudgetId(budget.getId());

        BudgetAlertHandler chain = new Warning50Handler();
        chain.setNext(new Warning80Handler()).setNext(new Exceeded100Handler());

        List<AlertType> newAlerts = new ArrayList<>();
        chain.handle(budget, spent, sentAlerts, newAlerts);

        newAlerts.forEach(alertType -> {
            budgetAlertRepository.save(BudgetAlert.of(budget, alertType));
            log.info("[BudgetAlert] 예산 알림 발생 — userId={}, budgetId={}, alertType={}, spent={}, limit={}",
                    budget.getUserId(), budget.getId(), alertType, spent, budget.getAmount());
        });
    }
}
