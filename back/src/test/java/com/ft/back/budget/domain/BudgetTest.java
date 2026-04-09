package com.ft.back.budget.domain;

import com.ft.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static com.ft.back.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Budget 도메인 테스트")
class BudgetTest {

    @Nested
    @DisplayName("예산 생성")
    class Create {

        @Test
        @DisplayName("성공 - 예산이 정상 생성된다")
        void success_createBudget() {
            // given
            Long userId = 1L;
            Long categoryId = 10L;
            YearMonth yearMonth = YearMonth.of(2026, 4);
            BigDecimal amount = new BigDecimal("300000");

            // when
            Budget budget = Budget.create(userId, categoryId, yearMonth, amount);

            // then
            assertThat(budget.getYearMonth()).isEqualTo("2026-04");
            assertThat(budget.getAmount()).isEqualByComparingTo(new BigDecimal("300000"));
        }

        @Test
        @DisplayName("실패 - 금액이 0이면 예외 발생")
        void fail_zeroAmount_throwsException() {
            // given
            Long userId = 1L;
            Long categoryId = 10L;
            YearMonth yearMonth = YearMonth.of(2026, 4);
            BigDecimal zeroAmount = BigDecimal.ZERO;

            // when & then
            assertThatThrownBy(() -> Budget.create(userId, categoryId, yearMonth, zeroAmount))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(BUDGET_INVALID_AMOUNT));
        }
    }

    @Nested
    @DisplayName("소유자 검증")
    class ValidateOwner {

        @Test
        @DisplayName("성공 - 본인이면 예외 없음")
        void success_sameUser_noException() {
            // given
            Long userId = 1L;
            Budget budget = Budget.create(userId, 10L, YearMonth.of(2026, 4), new BigDecimal("300000"));

            // when & then
            assertThatNoException().isThrownBy(() -> budget.validateOwner(userId));
        }

        @Test
        @DisplayName("실패 - 다른 사용자면 예외 발생")
        void fail_differentUser_throwsException() {
            // given
            Long ownerId = 1L;
            Long otherId = 2L;
            Budget budget = Budget.create(ownerId, 10L, YearMonth.of(2026, 4), new BigDecimal("300000"));

            // when & then
            assertThatThrownBy(() -> budget.validateOwner(otherId))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(BUDGET_NO_ACCESS));
        }
    }

    @Nested
    @DisplayName("사용률 계산")
    class CalculateUsageRate {

        @Test
        @DisplayName("성공 - 지출 6만원 / 예산 10만원 = 60%")
        void success_calculate60Percent() {
            // given
            Budget budget = Budget.create(1L, 10L, YearMonth.of(2026, 4), new BigDecimal("100000"));
            BigDecimal spent = new BigDecimal("60000");

            // when
            BigDecimal usageRate = budget.calculateUsageRate(spent);

            // then
            assertThat(usageRate).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("성공 - 지출 0원 = 0%")
        void success_calculateZeroPercent() {
            // given
            Budget budget = Budget.create(1L, 10L, YearMonth.of(2026, 4), new BigDecimal("100000"));
            BigDecimal spent = BigDecimal.ZERO;

            // when
            BigDecimal usageRate = budget.calculateUsageRate(spent);

            // then
            assertThat(usageRate).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("실패 - 지출 금액이 음수면 예외 발생")
        void fail_negativeSpent_throwsException() {
            // given
            Budget budget = Budget.create(1L, 10L, YearMonth.of(2026, 4), new BigDecimal("100000"));
            BigDecimal negativeSpent = new BigDecimal("-1000");

            // when & then
            assertThatThrownBy(() -> budget.calculateUsageRate(negativeSpent))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(BUDGET_EXPENSE_INVALID_AMOUNT));
        }
    }

    @Nested
    @DisplayName("예산 초과 여부")
    class IsExceeded {

        @Test
        @DisplayName("성공 - 지출이 예산과 같으면 초과")
        void success_spentEqualsAmount_isExceeded() {
            // given
            Budget budget = Budget.create(1L, 10L, YearMonth.of(2026, 4), new BigDecimal("100000"));
            BigDecimal spent = new BigDecimal("100000");

            // when
            boolean exceeded = budget.isExceeded(spent);

            // then
            assertThat(exceeded).isTrue();
        }

        @Test
        @DisplayName("성공 - 지출이 예산 미만이면 초과 아님")
        void success_spentLessThanAmount_notExceeded() {
            // given
            Budget budget = Budget.create(1L, 10L, YearMonth.of(2026, 4), new BigDecimal("100000"));
            BigDecimal spent = new BigDecimal("99999");

            // when
            boolean exceeded = budget.isExceeded(spent);

            // then
            assertThat(exceeded).isFalse();
        }
    }
}
