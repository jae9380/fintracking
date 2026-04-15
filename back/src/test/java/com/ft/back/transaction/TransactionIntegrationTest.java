package com.ft.back.transaction;

import com.ft.back.account.application.AccountService;
import com.ft.back.account.application.dto.AccountResult;
import com.ft.back.account.application.dto.CreateAccountCommand;
import com.ft.back.account.application.port.AccountRepository;
import com.ft.back.account.domain.Account;
import com.ft.back.account.domain.AccountType;
import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import com.ft.back.transaction.application.CategoryService;
import com.ft.back.transaction.application.TransactionService;
import com.ft.back.transaction.application.dto.CreateCategoryCommand;
import com.ft.back.transaction.application.dto.CreateTransactionCommand;
import com.ft.back.transaction.application.dto.TransactionResult;
import com.ft.back.transaction.domain.CategoryType;
import com.ft.back.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Transaction 통합 테스트")
class TransactionIntegrationTest {

    @Autowired private TransactionService transactionService;
    @Autowired private AccountService accountService;
    @Autowired private CategoryService categoryService;
    @Autowired private AccountRepository accountRepository;

    private static final Long USER_ID = 1L;
    private Long accountId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        AccountResult account = accountService.create(USER_ID,
                new CreateAccountCommand("테스트계좌", "0000-1234", AccountType.CHECKING));
        accountId = account.id();

        // 초기 잔액 입금 (INCOME 거래로)
        categoryId = categoryService.create(USER_ID,
                new CreateCategoryCommand("급여", CategoryType.INCOME)).id();
        transactionService.create(USER_ID, new CreateTransactionCommand(
                accountId, categoryId, null, TransactionType.INCOME,
                new BigDecimal("500000"), "초기 잔액", LocalDate.now()));
    }

    @Test
    @DisplayName("지출 거래 생성 시 계좌 잔액이 감소한다")
    void create_whenExpense_balanceDecreases() {
        // given
        Long expenseCategoryId = categoryService.create(USER_ID,
                new CreateCategoryCommand("식비", CategoryType.EXPENSE)).id();
        CreateTransactionCommand command = new CreateTransactionCommand(
                accountId, expenseCategoryId, null, TransactionType.EXPENSE,
                new BigDecimal("30000"), "점심", LocalDate.now());

        // when
        transactionService.create(USER_ID, command);

        // then
        Account account = accountRepository.findById(accountId).orElseThrow();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("470000"));
    }

    @Test
    @DisplayName("지출 거래 삭제 시 계좌 잔액이 복구된다")
    void delete_whenExpense_balanceRestored() {
        // given
        Long expenseCategoryId = categoryService.create(USER_ID,
                new CreateCategoryCommand("교통", CategoryType.EXPENSE)).id();
        TransactionResult created = transactionService.create(USER_ID, new CreateTransactionCommand(
                accountId, expenseCategoryId, null, TransactionType.EXPENSE,
                new BigDecimal("50000"), "교통비", LocalDate.now()));

        // when
        transactionService.delete(USER_ID, created.id());

        // then
        Account account = accountRepository.findById(accountId).orElseThrow();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("500000"));
    }

    @Test
    @DisplayName("다른 사용자 거래 삭제 시 예외 발생")
    void delete_whenNotOwner_throwsCustomException() {
        // given
        Long expenseCategoryId = categoryService.create(USER_ID,
                new CreateCategoryCommand("식비", CategoryType.EXPENSE)).id();
        TransactionResult created = transactionService.create(USER_ID, new CreateTransactionCommand(
                accountId, expenseCategoryId, null, TransactionType.EXPENSE,
                new BigDecimal("10000"), "커피", LocalDate.now()));

        // when & then
        assertThatThrownBy(() -> transactionService.delete(99L, created.id()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.TRANSACTION_NO_ACCESS));
    }
}
