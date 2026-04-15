package com.ft.back.account.application;

import com.ft.back.account.application.dto.AccountResult;
import com.ft.back.account.application.dto.CreateAccountCommand;
import com.ft.back.account.application.port.AccountRepository;
import com.ft.back.account.domain.Account;
import com.ft.back.account.domain.AccountType;
import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 단위 테스트")
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Nested
    @DisplayName("계좌 생성")
    class Create {

        @Test
        @DisplayName("성공 - 저장된 계좌 결과를 반환한다")
        void create_whenValidCommand_returnsAccountResult() {
            // given
            Long userId = 1L;
            CreateAccountCommand command = new CreateAccountCommand("카카오뱅크", "1234-5678", AccountType.CHECKING);
            Account account = Account.create(userId, command.accountName(), command.accountNumber(), command.accountType());
            given(accountRepository.save(any())).willReturn(account);

            // when
            AccountResult result = accountService.create(userId, command);

            // then
            assertThat(result.accountName()).isEqualTo("카카오뱅크");
            assertThat(result.accountType()).isEqualTo(AccountType.CHECKING);
        }

        @Test
        @DisplayName("실패 - 계좌 타입이 null이면 예외 발생")
        void create_whenNullType_throwsCustomException() {
            // given
            Long userId = 1L;
            CreateAccountCommand command = new CreateAccountCommand("카카오뱅크", "1234-5678", null);

            // when & then
            assertThatThrownBy(() -> accountService.create(userId, command))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_INVALID_TYPE));
        }
    }

    @Nested
    @DisplayName("계좌 목록 조회")
    class FindAll {

        @Test
        @DisplayName("성공 - 사용자의 계좌 목록을 반환한다")
        void findAll_whenAccountsExist_returnsAccountList() {
            // given
            Long userId = 1L;
            List<Account> accounts = List.of(
                    Account.create(userId, "카카오뱅크", "1111", AccountType.CHECKING),
                    Account.create(userId, "토스뱅크", "2222", AccountType.SAVINGS)
            );
            given(accountRepository.findAllByUserId(userId)).willReturn(accounts);

            // when
            List<AccountResult> results = accountService.findAll(userId);

            // then
            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("계좌 단건 조회")
    class FindById {

        @Test
        @DisplayName("성공 - 소유자이면 결과를 반환한다")
        void findById_whenOwner_returnsAccountResult() {
            // given
            Long userId = 1L;
            Long accountId = 10L;
            Account account = Account.create(userId, "카카오뱅크", "1234", AccountType.CHECKING);
            given(accountRepository.findById(accountId)).willReturn(Optional.of(account));

            // when
            AccountResult result = accountService.findById(userId, accountId);

            // then
            assertThat(result.accountName()).isEqualTo("카카오뱅크");
        }

        @Test
        @DisplayName("실패 - 계좌가 없으면 예외 발생")
        void findById_whenNotFound_throwsCustomException() {
            // given
            given(accountRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.findById(1L, 99L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 다른 사용자이면 예외 발생")
        void findById_whenNotOwner_throwsCustomException() {
            // given
            Long ownerId = 1L;
            Long otherId = 2L;
            Account account = Account.create(ownerId, "카카오뱅크", "1234", AccountType.CHECKING);
            given(accountRepository.findById(any())).willReturn(Optional.of(account));

            // when & then
            assertThatThrownBy(() -> accountService.findById(otherId, 10L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_OWNER_MISMATCH));
        }
    }

    @Nested
    @DisplayName("계좌 삭제")
    class Delete {

        @Test
        @DisplayName("성공 - 소유자이면 삭제를 호출한다")
        void delete_whenOwner_callsDeleteOnce() {
            // given
            Long userId = 1L;
            Long accountId = 10L;
            Account account = Account.create(userId, "카카오뱅크", "1234", AccountType.CHECKING);
            given(accountRepository.findById(accountId)).willReturn(Optional.of(account));

            // when
            accountService.delete(userId, accountId);

            // then
            then(accountRepository).should().delete(account);
        }

        @Test
        @DisplayName("실패 - 계좌가 없으면 예외 발생")
        void delete_whenNotFound_throwsCustomException() {
            // given
            given(accountRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.delete(1L, 99L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
        }
    }
}
