package com.ft.back.budget.domain;

import com.ft.back.budget.domain.handler.Exceeded100Handler;
import com.ft.back.budget.domain.handler.Warning50Handler;
import com.ft.back.budget.domain.handler.Warning80Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BudgetAlertChain 도메인 테스트")
class BudgetAlertChainTest {

    private Warning50Handler chain;
    private Budget budget;

    @BeforeEach
    void setUp() {
        // 체인 구성: 50% → 80% → 100%
        Warning50Handler w50 = new Warning50Handler();
        Warning80Handler w80 = new Warning80Handler();
        Exceeded100Handler e100 = new Exceeded100Handler();
        w50.setNext(w80).setNext(e100);
        chain = w50;

        budget = Budget.create(1L, 10L, YearMonth.of(2026, 4), new BigDecimal("100000"));
    }

    @Nested
    @DisplayName("알림 발생 조건")
    class AlertConditions {

        @Test
        @DisplayName("50% 미만 - 알림 없음")
        void noAlert_below50Percent() {
            // given
            BigDecimal spent = new BigDecimal("49000");
            List<AlertType> sentAlerts = new ArrayList<>();
            List<AlertType> result = new ArrayList<>();

            // when
            chain.handle(budget, spent, sentAlerts, result);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("50% 이상 80% 미만 - WARNING_50 발생")
        void alert_50to79Percent() {
            // given
            BigDecimal spent = new BigDecimal("60000");
            List<AlertType> sentAlerts = new ArrayList<>();
            List<AlertType> result = new ArrayList<>();

            // when
            chain.handle(budget, spent, sentAlerts, result);

            // then
            assertThat(result).containsExactly(AlertType.WARNING_50);
        }

        @Test
        @DisplayName("80% 이상 100% 미만 - WARNING_50, WARNING_80 발생")
        void alert_80to99Percent() {
            // given
            BigDecimal spent = new BigDecimal("85000");
            List<AlertType> sentAlerts = new ArrayList<>();
            List<AlertType> result = new ArrayList<>();

            // when
            chain.handle(budget, spent, sentAlerts, result);

            // then
            assertThat(result).containsExactly(AlertType.WARNING_50, AlertType.WARNING_80);
        }

        @Test
        @DisplayName("100% 초과 - WARNING_50, WARNING_80, EXCEEDED_100 발생")
        void alert_100PercentOrMore() {
            // given
            BigDecimal spent = new BigDecimal("100000");
            List<AlertType> sentAlerts = new ArrayList<>();
            List<AlertType> result = new ArrayList<>();

            // when
            chain.handle(budget, spent, sentAlerts, result);

            // then
            assertThat(result).containsExactly(
                    AlertType.WARNING_50,
                    AlertType.WARNING_80,
                    AlertType.EXCEEDED_100
            );
        }
    }

    @Nested
    @DisplayName("중복 알림 방지")
    class DuplicatePrevention {

        @Test
        @DisplayName("이미 전송된 WARNING_50은 다시 발생하지 않는다")
        void noAlert_alreadySentWarning50() {
            // given
            BigDecimal spent = new BigDecimal("60000");
            List<AlertType> sentAlerts = List.of(AlertType.WARNING_50);
            List<AlertType> result = new ArrayList<>();

            // when
            chain.handle(budget, spent, sentAlerts, result);

            // then
            assertThat(result).doesNotContain(AlertType.WARNING_50);
        }

        @Test
        @DisplayName("80%에서 WARNING_50 전송 완료 상태 - WARNING_80만 새로 발생")
        void onlyWarning80_whenWarning50AlreadySent() {
            // given
            BigDecimal spent = new BigDecimal("85000");
            List<AlertType> sentAlerts = new ArrayList<>(List.of(AlertType.WARNING_50));
            List<AlertType> result = new ArrayList<>();

            // when
            chain.handle(budget, spent, sentAlerts, result);

            // then
            assertThat(result).containsExactly(AlertType.WARNING_80);
        }
    }
}
