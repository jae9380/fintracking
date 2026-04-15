package com.ft.back.account.application.port;

import com.ft.back.account.domain.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findAllByUserId(Long userId);
    void delete(Account account);
}
