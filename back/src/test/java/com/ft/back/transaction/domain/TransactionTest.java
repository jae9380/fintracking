package com.ft.back.transaction.domain;

import com.ft.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.ft.back.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Transaction 도메인 테스트")
class TransactionTest {

    @Nested
    @DisplayName("거래 생성")
    class Create {

        @Test
        @DisplayName("성공 - 수입 거래가 정상 생성된다")
        void success_createIncomeTransaction() {
            // given
            Long userId = 1L;
            Long accountId = 10L;
            Long categoryId = 100L;
            TransactionType type = TransactionType.INCOME;
            BigDecimal amount = new BigDecimal("50000");
            LocalDate date = LocalDate.now();

            // when
            Transaction transaction = Transaction.create(userId, accountId, null, categoryId, type, amount, "급여", date);

            // then
            assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("성공 - 이체 거래는 카테고리 없이 생성 가능")
        void success_createTransferWithoutCategory() {
            // given
            Long userId = 1L;
            Long accountId = 10L;
            Long toAccountId = 20L;
            Long categoryId = null;
            TransactionType type = TransactionType.TRANSFER;
            BigDecimal amount = new BigDecimal("10000");
            LocalDate date = LocalDate.now();

            // when
            Transaction transaction = Transaction.create(userId, accountId, toAccountId, categoryId, type, amount, "이체", date);

            // then
            assertThat(transaction.getType()).isEqualTo(TransactionType.TRANSFER);
            assertThat(transaction.getCategoryId()).isNull();
            assertThat(transaction.getToAccountId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("실패 - 수입/지출 거래에 카테고리가 없으면 예외 발생")
        void fail_incomeWithoutCategory_throwsException() {
            // given
            Long userId = 1L;
            Long accountId = 10L;
            Long categoryId = null;
            TransactionType type = TransactionType.EXPENSE;
            BigDecimal amount = new BigDecimal("10000");
            LocalDate date = LocalDate.now();

            // when & then
            assertThatThrownBy(() -> Transaction.create(userId, accountId, null, categoryId, type, amount, "식비", date))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(TRANSACTION_CATEGORY_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 금액이 0이면 예외 발생")
        void fail_zeroAmount_throwsException() {
            // given
            Long userId = 1L;
            Long accountId = 10L;
            Long categoryId = 100L;
            BigDecimal zeroAmount = BigDecimal.ZERO;
            LocalDate date = LocalDate.now();

            // when & then
            assertThatThrownBy(() -> Transaction.create(userId, accountId, null, categoryId, TransactionType.EXPENSE, zeroAmount, "식비", date))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(TRANSACTION_INVALID_AMOUNT));
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
            Transaction transaction = Transaction.create(userId, 10L, null, 100L, TransactionType.EXPENSE,
                    new BigDecimal("5000"), "식비", LocalDate.now());

            // when & then
            assertThatNoException().isThrownBy(() -> transaction.validateOwner(userId));
        }

        @Test
        @DisplayName("실패 - 다른 사용자면 예외 발생")
        void fail_differentUser_throwsException() {
            // given
            Long ownerId = 1L;
            Long otherId = 2L;
            Transaction transaction = Transaction.create(ownerId, 10L, null, 100L, TransactionType.EXPENSE,
                    new BigDecimal("5000"), "식비", LocalDate.now());

            // when & then
            assertThatThrownBy(() -> transaction.validateOwner(otherId))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(TRANSACTION_NO_ACCESS));
        }
    }

    @Nested
    @DisplayName("거래 수정")
    class Update {

        @Test
        @DisplayName("성공 - 금액 수정")
        void success_updateAmount() {
            // given
            Transaction transaction = Transaction.create(1L, 10L, null, 100L, TransactionType.EXPENSE,
                    new BigDecimal("5000"), "식비", LocalDate.now());
            BigDecimal newAmount = new BigDecimal("8000");

            // when
            transaction.updateAmount(newAmount);

            // then
            assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("8000"));
        }

        @Test
        @DisplayName("실패 - 수정 금액이 0이면 예외 발생")
        void fail_updateZeroAmount_throwsException() {
            // given
            Transaction transaction = Transaction.create(1L, 10L, null, 100L, TransactionType.EXPENSE,
                    new BigDecimal("5000"), "식비", LocalDate.now());

            // when & then
            assertThatThrownBy(() -> transaction.updateAmount(BigDecimal.ZERO))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(TRANSACTION_INVALID_AMOUNT));
        }

        @Test
        @DisplayName("성공 - 거래일 수정")
        void success_updateTransactionDate() {
            // given
            Transaction transaction = Transaction.create(1L, 10L, null, 100L, TransactionType.EXPENSE,
                    new BigDecimal("5000"), "식비", LocalDate.now());
            LocalDate newDate = LocalDate.of(2026, 1, 1);

            // when
            transaction.updateTransactionDate(newDate);

            // then
            assertThat(transaction.getTransactionDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        }
    }
}
