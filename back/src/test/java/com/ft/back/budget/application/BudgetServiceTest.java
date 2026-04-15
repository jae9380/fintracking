package com.ft.back.budget.application;

import com.ft.back.budget.application.dto.BudgetResult;
import com.ft.back.budget.application.dto.CreateBudgetCommand;
import com.ft.back.budget.application.port.BudgetAlertRepository;
import com.ft.back.budget.application.port.BudgetRepository;
import com.ft.back.budget.domain.AlertType;
import com.ft.back.budget.domain.Budget;
import com.ft.back.budget.domain.BudgetAlert;
import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import com.ft.back.notification.application.NotificationService;
import com.ft.back.transaction.application.port.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetService 단위 테스트")
class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetAlertRepository budgetAlertRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private NotificationService notificationService;

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 5L;
    private static final YearMonth YEAR_MONTH = YearMonth.of(2026, 4);

    @Nested
    @DisplayName("예산 등록")
    class Create {

        @Test
        @DisplayName("성공 - 중복 없으면 예산을 저장하고 반환한다")
        void create_whenNoDuplicate_returnsBudgetResult() {
            // given
            CreateBudgetCommand command = new CreateBudgetCommand(CATEGORY_ID, YEAR_MONTH, new BigDecimal("300000"));
            Budget budget = Budget.create(USER_ID, CATEGORY_ID, YEAR_MONTH, new BigDecimal("300000"));
            given(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(anyLong(), anyLong(), anyString()))
                    .willReturn(Optional.empty());
            given(budgetRepository.save(any())).willReturn(budget);

            // when
            BudgetResult result = budgetService.create(USER_ID, command);

            // then
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(result.spent()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("실패 - 같은 카테고리/월 예산이 존재하면 예외 발생")
        void create_whenDuplicate_throwsCustomException() {
            // given
            CreateBudgetCommand command = new CreateBudgetCommand(CATEGORY_ID, YEAR_MONTH, new BigDecimal("300000"));
            Budget existing = Budget.create(USER_ID, CATEGORY_ID, YEAR_MONTH, new BigDecimal("200000"));
            given(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(anyLong(), anyLong(), anyString()))
                    .willReturn(Optional.of(existing));

            // when & then
            assertThatThrownBy(() -> budgetService.create(USER_ID, command))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BUDGET_DUPLICATE));
        }
    }

    @Nested
    @DisplayName("예산 금액 수정")
    class UpdateAmount {

        @Test
        @DisplayName("성공 - 금액이 수정된다")
        void updateAmount_whenOwner_updatesAmount() {
            // given
            Budget budget = Budget.create(USER_ID, CATEGORY_ID, YEAR_MONTH, new BigDecimal("200000"));
            given(budgetRepository.findById(anyLong())).willReturn(Optional.of(budget));
            given(transactionRepository.sumExpenseByUserIdAndCategoryIdAndYearMonth(anyLong(), anyLong(), anyInt(), anyInt()))
                    .willReturn(BigDecimal.ZERO);
            given(budgetAlertRepository.findSentAlertTypesByBudgetId(any())).willReturn(List.of());

            // when
            BudgetResult result = budgetService.updateAmount(USER_ID, 1L, new BigDecimal("500000"));

            // then
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("500000"));
        }
    }

    @Nested
    @DisplayName("예산 삭제")
    class Delete {

        @Test
        @DisplayName("성공 - 소유자이면 삭제를 호출한다")
        void delete_whenOwner_callsDeleteOnce() {
            // given
            Budget budget = Budget.create(USER_ID, CATEGORY_ID, YEAR_MONTH, new BigDecimal("200000"));
            given(budgetRepository.findById(anyLong())).willReturn(Optional.of(budget));

            // when
            budgetService.delete(USER_ID, 1L);

            // then
            then(budgetRepository).should().delete(budget);
        }

        @Test
        @DisplayName("실패 - 예산이 없으면 예외 발생")
        void delete_whenNotFound_throwsCustomException() {
            // given
            given(budgetRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> budgetService.delete(USER_ID, 99L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BUDGET_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("알림 체크")
    class CheckAlerts {

        @Test
        @DisplayName("성공 - 50% 초과 시 WARNING_50 알림이 발생한다")
        void checkAlerts_whenOver50Percent_returnsWarning50() {
            // given
            Budget budget = Budget.create(USER_ID, CATEGORY_ID, YEAR_MONTH, new BigDecimal("100000"));
            given(budgetRepository.findById(anyLong())).willReturn(Optional.of(budget));
            given(transactionRepository.sumExpenseByUserIdAndCategoryIdAndYearMonth(anyLong(), anyLong(), anyInt(), anyInt()))
                    .willReturn(new BigDecimal("60000"));
            given(budgetAlertRepository.findSentAlertTypesByBudgetId(any())).willReturn(List.of());
            given(budgetAlertRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            List<AlertType> alerts = budgetService.checkAlerts(USER_ID, 1L);

            // then
            assertThat(alerts).contains(AlertType.WARNING_50);
        }

        @Test
        @DisplayName("성공 - 이미 전송된 알림은 중복 발생하지 않는다")
        void checkAlerts_whenAlreadySent_doesNotDuplicate() {
            // given
            Budget budget = Budget.create(USER_ID, CATEGORY_ID, YEAR_MONTH, new BigDecimal("100000"));
            given(budgetRepository.findById(anyLong())).willReturn(Optional.of(budget));
            given(transactionRepository.sumExpenseByUserIdAndCategoryIdAndYearMonth(anyLong(), anyLong(), anyInt(), anyInt()))
                    .willReturn(new BigDecimal("60000"));
            given(budgetAlertRepository.findSentAlertTypesByBudgetId(any()))
                    .willReturn(List.of(AlertType.WARNING_50));

            // when
            List<AlertType> alerts = budgetService.checkAlerts(USER_ID, 1L);

            // then
            assertThat(alerts).doesNotContain(AlertType.WARNING_50);
        }
    }
}
