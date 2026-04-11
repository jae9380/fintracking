package com.ft.back.transaction.application;

import com.ft.back.account.application.port.AccountRepository;
import com.ft.back.account.domain.Account;
import com.ft.back.common.exception.CustomException;
import com.ft.back.transaction.application.dto.CreateTransactionCommand;
import com.ft.back.transaction.application.dto.TransactionResult;
import com.ft.back.transaction.application.port.CategoryRepository;
import com.ft.back.transaction.application.port.TransactionRepository;
import com.ft.back.transaction.domain.Category;
import com.ft.back.transaction.domain.Transaction;
import com.ft.back.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ft.back.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionResult create(Long userId, CreateTransactionCommand command) {
        Account account = getAccount(command.accountId());
        account.validateOwner(userId);

        if (command.categoryId() != null) {
            Category category = categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new CustomException(TRANSACTION_NOT_FOUND));
            category.validateAccessible(userId);
        }

        Transaction transaction = Transaction.create(
                userId,
                command.accountId(),
                command.categoryId(),
                command.type(),
                command.amount(),
                command.description(),
                command.transactionDate()
        );

        updateAccountBalance(userId, account, command);

        return TransactionResult.from(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResult> findAll(Long userId, Long accountId) {
        List<Transaction> transactions = accountId != null
                ? transactionRepository.findAllByUserIdAndAccountId(userId, accountId)
                : transactionRepository.findAllByUserId(userId);
        return transactions.stream().map(TransactionResult::from).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResult findById(Long userId, Long transactionId) {
        Transaction transaction = getTransaction(transactionId);
        transaction.validateOwner(userId);
        return TransactionResult.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Transaction transaction = getTransaction(transactionId);
        transaction.validateOwner(userId);
        transactionRepository.delete(transaction);
    }

    private void updateAccountBalance(Long userId, Account account, CreateTransactionCommand command) {
        if (command.type() == TransactionType.INCOME) {
            account.deposit(command.amount());
        } else if (command.type() == TransactionType.EXPENSE) {
            account.withdraw(command.amount());
        } else if (command.type() == TransactionType.TRANSFER && command.toAccountId() != null) {
            account.withdraw(command.amount());
            Account toAccount = getAccount(command.toAccountId());
            toAccount.validateOwner(userId);
            toAccount.deposit(command.amount());
        }
    }

    private Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(TRANSACTION_NOT_FOUND));
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
    }
}
