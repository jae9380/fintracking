package com.ft.back.budget.application;

import com.ft.back.budget.application.dto.BudgetResult;
import com.ft.back.budget.application.dto.CreateBudgetCommand;
import com.ft.back.budget.application.port.BudgetAlertRepository;
import com.ft.back.budget.application.port.BudgetRepository;
import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.domain.Budget;
import com.ft.back.budget.domain.BudgetAlert;
import com.ft.back.budget.domain.handler.BudgetAlertHandler;
import com.ft.back.budget.domain.handler.Exceeded100Handler;
import com.ft.back.budget.domain.handler.Warning50Handler;
import com.ft.back.budget.domain.handler.Warning80Handler;
import com.ft.back.common.exception.CustomException;
import com.ft.back.transaction.application.port.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static com.ft.back.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final TransactionRepository transactionRepository;

    // 예산 생성
    @Transactional
    public BudgetResult create(Long userId, CreateBudgetCommand command) {
        budgetRepository.findByUserIdAndCategoryIdAndYearMonth(
                        userId, command.categoryId(), command.yearMonth().toString())
                .ifPresent(b -> { throw new CustomException(BUDGET_DUPLICATE); });

        Budget budget = Budget.create(userId, command.categoryId(), command.yearMonth(), command.amount());
        Budget saved = budgetRepository.save(budget);

        return BudgetResult.of(saved, BigDecimal.ZERO, List.of());
    }

    // 월별 예산 목록 조회
    @Transactional(readOnly = true)
    public List<BudgetResult> findAll(Long userId, YearMonth yearMonth) {
        return budgetRepository.findAllByUserIdAndYearMonth(userId, yearMonth.toString())
                .stream()
                .map(budget -> buildResult(budget))
                .toList();
    }

    // 예산 단건 조회
    @Transactional(readOnly = true)
    public BudgetResult findById(Long userId, Long budgetId) {
        Budget budget = getBudget(budgetId);
        budget.validateOwner(userId);
        return buildResult(budget);
    }

    // 예산 금액 수정
    @Transactional
    public BudgetResult updateAmount(Long userId, Long budgetId, BigDecimal amount) {
        Budget budget = getBudget(budgetId);
        budget.validateOwner(userId);
        budget.updateAmount(amount);
        return buildResult(budget);
    }

    // 예산 삭제
    @Transactional
    public void delete(Long userId, Long budgetId) {
        Budget budget = getBudget(budgetId);
        budget.validateOwner(userId);
        budgetRepository.delete(budget);
    }

    // 알림 체크 (Chain of Responsibility 실행)
    @Transactional
    public List<AlertType> checkAlerts(Long userId, Long budgetId) {
        Budget budget = getBudget(budgetId);
        budget.validateOwner(userId);

        YearMonth yearMonth = YearMonth.parse(budget.getYearMonth());
        BigDecimal spent = transactionRepository.sumExpenseByUserIdAndCategoryIdAndYearMonth(
                userId, budget.getCategoryId(), yearMonth.getYear(), yearMonth.getMonthValue());

        List<AlertType> sentAlerts = budgetAlertRepository.findSentAlertTypesByBudgetId(budgetId);

        // 체인 구성: 50% → 80% → 100%
        BudgetAlertHandler chain = new Warning50Handler();
        chain.setNext(new Warning80Handler()).setNext(new Exceeded100Handler());

        List<AlertType> newAlerts = new ArrayList<>();
        chain.handle(budget, spent, sentAlerts, newAlerts);

        // 새로 발생한 알림 저장
        newAlerts.forEach(alertType ->
                budgetAlertRepository.save(BudgetAlert.of(budget, alertType)));

        return newAlerts;
    }

    private BudgetResult buildResult(Budget budget) {
        YearMonth yearMonth = YearMonth.parse(budget.getYearMonth());
        BigDecimal spent = transactionRepository.sumExpenseByUserIdAndCategoryIdAndYearMonth(
                budget.getUserId(), budget.getCategoryId(),
                yearMonth.getYear(), yearMonth.getMonthValue());
        List<AlertType> sentAlerts = budgetAlertRepository.findSentAlertTypesByBudgetId(budget.getId());
        return BudgetResult.of(budget, spent, sentAlerts);
    }

    private Budget getBudget(Long budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new CustomException(BUDGET_NOT_FOUND));
    }
}
