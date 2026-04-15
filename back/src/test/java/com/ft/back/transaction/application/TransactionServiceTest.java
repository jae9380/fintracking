package com.ft.back.transaction.application;

import com.ft.back.account.application.port.AccountRepository;
import com.ft.back.account.domain.Account;
import com.ft.back.account.domain.AccountType;
import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import com.ft.back.transaction.application.dto.CreateTransactionCommand;
import com.ft.back.transaction.application.dto.TransactionResult;
import com.ft.back.transaction.application.dto.UpdateTransactionCommand;
import com.ft.back.transaction.application.event.TransactionCreatedEvent;
import com.ft.back.transaction.application.port.CategoryRepository;
import com.ft.back.transaction.application.port.TransactionRepository;
import com.ft.back.transaction.domain.Category;
import com.ft.back.transaction.domain.CategoryType;
import com.ft.back.transaction.domain.Transaction;
import com.ft.back.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 단위 테스트")
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private Account account;
    private static final Long USER_ID = 1L;
    private static final Long ACCOUNT_ID = 10L;

    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        account = Account.create(USER_ID, "카카오뱅크", "1234-5678", AccountType.CHECKING);
        account.deposit(new BigDecimal("100000"));
        expenseCategory = Category.create(USER_ID, "식비", CategoryType.EXPENSE);
    }

    @Nested
    @DisplayName("거래 생성")
    class Create {

        @Test
        @DisplayName("성공 - 수입 생성 시 계좌 잔액이 증가한다")
        void create_whenIncome_depositsToAccount() {
            // given
            CreateTransactionCommand command = new CreateTransactionCommand(
                    ACCOUNT_ID, 1L, null, TransactionType.INCOME,
                    new BigDecimal("50000"), "급여", LocalDate.now());
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(expenseCategory));
            given(transactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            transactionService.create(USER_ID, command);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("150000"));
        }

        @Test
        @DisplayName("성공 - 지출 생성 시 계좌 잔액이 감소한다")
        void create_whenExpense_withdrawsFromAccount() {
            // given
            CreateTransactionCommand command = new CreateTransactionCommand(
                    ACCOUNT_ID, 1L, null, TransactionType.EXPENSE,
                    new BigDecimal("30000"), "식비", LocalDate.now());
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(expenseCategory));
            given(transactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            transactionService.create(USER_ID, command);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("70000"));
        }

        @Test
        @DisplayName("성공 - 거래 생성 후 TransactionCreatedEvent가 발행된다")
        void create_whenExpense_publishesTransactionCreatedEvent() {
            // given
            CreateTransactionCommand command = new CreateTransactionCommand(
                    ACCOUNT_ID, 1L, null, TransactionType.EXPENSE,
                    new BigDecimal("20000"), "교통", LocalDate.now());
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(expenseCategory));
            given(transactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<TransactionCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);

            // when
            transactionService.create(USER_ID, command);

            // then
            then(eventPublisher).should().publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().type()).isEqualTo(TransactionType.EXPENSE);
            assertThat(eventCaptor.getValue().userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("실패 - 잔액 부족 시 예외 발생")
        void create_whenInsufficientBalance_throwsCustomException() {
            // given
            CreateTransactionCommand command = new CreateTransactionCommand(
                    ACCOUNT_ID, 1L, null, TransactionType.EXPENSE,
                    new BigDecimal("200000"), "과소비", LocalDate.now());
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(expenseCategory));

            // when & then
            assertThatThrownBy(() -> transactionService.create(USER_ID, command))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE));
        }
    }

    @Nested
    @DisplayName("거래 수정")
    class Update {

        @Test
        @DisplayName("성공 - 지출 금액 증가 시 추가 금액이 출금된다")
        void update_whenExpenseAmountIncreased_withdrawsAdditionalAmount() {
            // given
            Transaction transaction = Transaction.create(
                    USER_ID, ACCOUNT_ID, null, 1L,
                    TransactionType.EXPENSE, new BigDecimal("30000"), "식비", LocalDate.now());
            given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));

            UpdateTransactionCommand command = new UpdateTransactionCommand(new BigDecimal("50000"), null, null);

            // when
            transactionService.update(USER_ID, 1L, command);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("80000"));
        }

        @Test
        @DisplayName("성공 - 지출 금액 감소 시 차액이 입금된다")
        void update_whenExpenseAmountDecreased_depositsRefundAmount() {
            // given
            Transaction transaction = Transaction.create(
                    USER_ID, ACCOUNT_ID, null, 1L,
                    TransactionType.EXPENSE, new BigDecimal("50000"), "식비", LocalDate.now());
            given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));

            UpdateTransactionCommand command = new UpdateTransactionCommand(new BigDecimal("20000"), null, null);

            // when
            transactionService.update(USER_ID, 1L, command);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("130000"));
        }
    }

    @Nested
    @DisplayName("거래 삭제")
    class Delete {

        @Test
        @DisplayName("성공 - 지출 삭제 시 계좌 잔액이 복구된다")
        void delete_whenExpense_restoresBalanceByDeposit() {
            // given
            Transaction transaction = Transaction.create(
                    USER_ID, ACCOUNT_ID, null, 1L,
                    TransactionType.EXPENSE, new BigDecimal("40000"), "식비", LocalDate.now());
            given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));

            // when
            transactionService.delete(USER_ID, 1L);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("140000"));
            then(transactionRepository).should().delete(transaction);
        }

        @Test
        @DisplayName("성공 - 수입 삭제 시 계좌 잔액이 차감된다")
        void delete_whenIncome_restoresBalanceByWithdraw() {
            // given
            Transaction transaction = Transaction.create(
                    USER_ID, ACCOUNT_ID, null, 1L,
                    TransactionType.INCOME, new BigDecimal("30000"), "급여", LocalDate.now());
            given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(account));

            // when
            transactionService.delete(USER_ID, 1L);

            // then
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("70000"));
        }

        @Test
        @DisplayName("실패 - 거래가 없으면 예외 발생")
        void delete_whenNotFound_throwsCustomException() {
            // given
            given(transactionRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> transactionService.delete(USER_ID, 99L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND));
        }
    }
}
