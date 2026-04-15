package com.ft.back.batch.infrastructure.job;

import com.ft.back.auth.domain.User;
import com.ft.back.batch.domain.MonthlyStatistics;
import com.ft.back.transaction.domain.TransactionType;
import com.ft.back.transaction.infrastructure.persistence.JpaTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * User 한 명에 대해 전월 트랜잭션을 집계하여 MonthlyStatistics 목록을 생성한다.
 *
 * 반환 타입이 List<MonthlyStatistics>인 이유:
 * - 카테고리별로 row가 분리되므로 User 1명 → N개의 MonthlyStatistics가 생성된다.
 * - Spring Batch는 ItemProcessor<I, O>에서 O가 List여도 그대로 ItemWriter로 전달한다.
 * - ItemWriter에서 flatten 처리한다.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyStatisticsProcessor implements ItemProcessor<User, List<MonthlyStatistics>> {

    private final JpaTransactionRepository jpaTransactionRepository;

    // JobParameter "yearMonth" ("2026-03" 형식)를 주입받는다.
    @Value("#{jobParameters['yearMonth']}")
    private String yearMonthStr;

    @Override
    public List<MonthlyStatistics> process(User user) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        Long userId = user.getId();

        BigDecimal totalIncome = jpaTransactionRepository
                .sumByUserIdAndTypeAndYearMonth(userId, TransactionType.INCOME, year, month);
        BigDecimal totalExpense = jpaTransactionRepository
                .sumByUserIdAndTypeAndYearMonth(userId, TransactionType.EXPENSE, year, month);

        List<Object[]> categoryRows = jpaTransactionRepository
                .sumExpenseGroupedByCategoryAndYearMonth(userId, year, month);

        List<MonthlyStatistics> result = new ArrayList<>();

        if (categoryRows.isEmpty()) {
            // 거래 내역이 없는 유저도 전체 집계 row 하나는 저장
            result.add(MonthlyStatistics.create(
                    userId, yearMonthStr,
                    totalIncome, totalExpense,
                    null, BigDecimal.ZERO
            ));
        } else {
            for (Object[] row : categoryRows) {
                Long categoryId = (Long) row[0];
                BigDecimal categoryExpense = (BigDecimal) row[1];

                result.add(MonthlyStatistics.create(
                        userId, yearMonthStr,
                        totalIncome, totalExpense,
                        categoryId, categoryExpense
                ));
            }
        }

        log.debug("[Batch][Processor] 집계 완료 — userId={}, yearMonth={}, rows={}",
                userId, yearMonthStr, result.size());
        return result;
    }
}
