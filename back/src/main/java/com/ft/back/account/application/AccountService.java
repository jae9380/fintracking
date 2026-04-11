package com.ft.back.account.application;

import com.ft.back.account.application.dto.AccountResult;
import com.ft.back.account.application.dto.CreateAccountCommand;
import com.ft.back.account.application.factory.AccountFactory;
import com.ft.back.account.application.port.AccountRepository;
import com.ft.back.account.domain.Account;
import com.ft.back.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ft.back.common.exception.ErrorCode.ACCOUNT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    // 계좌 생성
    @Transactional
    public AccountResult create(Long userId, CreateAccountCommand command) {
        Account account = AccountFactory.create(userId, command);
        return AccountResult.from(accountRepository.save(account));
    }

    // 계좌 목록 조회
    @Transactional(readOnly = true)
    public List<AccountResult> findAll(Long userId) {
        return accountRepository.findAllByUserId(userId)
                .stream()
                .map(AccountResult::from)
                .toList();
    }

    // 계좌 단건 조회
    @Transactional(readOnly = true)
    public AccountResult findById(Long userId, Long accountId) {
        Account account = getAccount(accountId);
        account.validateOwner(userId);
        return AccountResult.from(account);
    }

    // 계좌 삭제
    @Transactional
    public void delete(Long userId, Long accountId) {
        Account account = getAccount(accountId);
        account.validateOwner(userId);
        accountRepository.delete(account);
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
    }
}
