package com.ft.back.account;

import com.ft.back.account.application.AccountService;
import com.ft.back.account.application.dto.AccountResult;
import com.ft.back.account.application.dto.CreateAccountCommand;
import com.ft.back.account.domain.AccountType;
import com.ft.back.common.exception.CustomException;
import com.ft.back.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Account 통합 테스트")
class AccountIntegrationTest {

    @Autowired
    private AccountService accountService;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("계좌 생성 후 목록 조회 시 저장된 계좌가 반환된다")
    void createAndFindAll_returnsCreatedAccount() {
        // given
        CreateAccountCommand command = new CreateAccountCommand("카카오뱅크", "1234-5678-9012", AccountType.CHECKING);

        // when
        accountService.create(USER_ID, command);
        List<AccountResult> results = accountService.findAll(USER_ID);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).accountName()).isEqualTo("카카오뱅크");
        assertThat(results.get(0).accountType()).isEqualTo(AccountType.CHECKING);
    }

    @Test
    @DisplayName("계좌번호는 마스킹되어 반환된다")
    void create_accountNumberIsMasked() {
        // given
        CreateAccountCommand command = new CreateAccountCommand("토스뱅크", "1234-5678-9012", AccountType.SAVINGS);

        // when
        AccountResult result = accountService.create(USER_ID, command);

        // then
        assertThat(result.maskedAccountNumber()).doesNotContain("5678");
        assertThat(result.maskedAccountNumber()).startsWith("1234");
    }

    @Test
    @DisplayName("계좌 삭제 후 조회 시 해당 계좌가 없다")
    void delete_thenFindAll_doesNotContainDeletedAccount() {
        // given
        AccountResult created = accountService.create(USER_ID,
                new CreateAccountCommand("삭제계좌", "9999-8888", AccountType.CHECKING));
        Long accountId = created.id();

        // when
        accountService.delete(USER_ID, accountId);
        List<AccountResult> results = accountService.findAll(USER_ID);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자 계좌 조회 시 예외 발생")
    void findById_whenNotOwner_throwsCustomException() {
        // given
        AccountResult created = accountService.create(USER_ID,
                new CreateAccountCommand("내계좌", "1111-2222", AccountType.CHECKING));
        Long accountId = created.id();
        Long anotherUserId = 99L;

        // when & then
        assertThatThrownBy(() -> accountService.findById(anotherUserId, accountId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCOUNT_OWNER_MISMATCH));
    }
}
