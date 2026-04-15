package com.ft.back.account.domain;

import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.ft.back.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Account 도메인 테스트")
class AccountTest {

    @Nested
    @DisplayName("계좌 생성")
    class Create {

        @Test
        @DisplayName("성공 - 초기 잔액은 0이다")
        void success_initialBalanceIsZero() {
            // given
            Long userId = 1L;
            String accountName = "카카오뱅크";
            String accountNumber = "1234-5678";
            AccountType accountType = AccountType.CHECKING;

            // when
            Account account = Account.create(userId, accountName, accountNumber, accountType);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getAccountType()).isEqualTo(AccountType.CHECKING);
        }

        @Test
        @DisplayName("실패 - 계좌명이 공백이면 예외 발생")
        void fail_blankName_throwsException() {
            // given
            Long userId = 1L;
            String blankName = " ";
            String accountNumber = "1234-5678";
            AccountType accountType = AccountType.CHECKING;

            // when & then
            assertThatThrownBy(() -> Account.create(userId, blankName, accountNumber, accountType))
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ACCOUNT_INVALID_NAME));
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
            Account account = Account.create(userId, "카카오뱅크", "1234-5678", AccountType.CHECKING);

            // when & then
            assertThatNoException().isThrownBy(() -> account.validateOwner(userId));
        }

        @Test
        @DisplayName("실패 - 다른 사용자면 예외 발생")
        void fail_differentUser_throwsException() {
            // given
            Long ownerId = 1L;
            Long otherId = 2L;
            Account account = Account.create(ownerId, "카카오뱅크", "1234-5678", AccountType.CHECKING);

            // when & then
            assertThatThrownBy(() -> account.validateOwner(otherId))
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ACCOUNT_OWNER_MISMATCH));
        }
    }

    @Nested
    @DisplayName("입금")
    class Deposit {

        @Test
        @DisplayName("성공 - 잔액이 증가한다")
        void success_balanceIncreases() {
            // given
            Account account = Account.create(1L, "카카오뱅크", "1234-5678", AccountType.CHECKING);
            BigDecimal depositAmount = new BigDecimal("10000");

            // when
            account.deposit(depositAmount);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("실패 - 0 이하 금액이면 예외 발생")
        void fail_zeroAmount_throwsException() {
            // given
            Account account = Account.create(1L, "카카오뱅크", "1234-5678", AccountType.CHECKING);
            BigDecimal zeroAmount = BigDecimal.ZERO;

            // when & then
            assertThatThrownBy(() -> account.deposit(zeroAmount))
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ACCOUNT_INVALID_AMOUNT));
        }
    }

    @Nested
    @DisplayName("출금")
    class Withdraw {

        @Test
        @DisplayName("성공 - 잔액이 감소한다")
        void success_balanceDecreases() {
            // given
            Account account = Account.create(1L, "카카오뱅크", "1234-5678", AccountType.CHECKING);
            account.deposit(new BigDecimal("10000"));
            BigDecimal withdrawAmount = new BigDecimal("3000");

            // when
            account.withdraw(withdrawAmount);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("7000"));
        }

        @Test
        @DisplayName("실패 - 잔액 부족이면 예외 발생")
        void fail_insufficientBalance_throwsException() {
            // given
            Account account = Account.create(1L, "카카오뱅크", "1234-5678", AccountType.CHECKING);
            account.deposit(new BigDecimal("5000"));
            BigDecimal withdrawAmount = new BigDecimal("10000");

            // when & then
            assertThatThrownBy(() -> account.withdraw(withdrawAmount))
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ACCOUNT_INSUFFICIENT_BALANCE));

        }
    }

    @Nested
    @DisplayName("계좌명 변경")
    class UpdateName {

        @Test
        @DisplayName("성공 - 계좌명이 변경된다")
        void success_nameUpdated() {
            // given
            Account account = Account.create(1L, "카카오뱅크", "1234-5678", AccountType.CHECKING);
            String newName = "토스뱅크";

            // when
            account.updateName(newName);

            // then
            assertThat(account.getAccountName()).isEqualTo("토스뱅크");
        }

        @Test
        @DisplayName("실패 - 공백이면 예외 발생")
        void fail_blankName_throwsException() {
            // given
            Account account = Account.create(1L, "카카오뱅크", "1234-5678", AccountType.CHECKING);
            String blankName = "";

            // when & then
            assertThatThrownBy(() -> account.updateName(blankName))
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ACCOUNT_INVALID_NAME));
        }
    }
}
