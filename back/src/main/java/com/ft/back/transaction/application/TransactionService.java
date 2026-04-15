package com.ft.back.transaction.application;

import com.ft.back.account.application.port.AccountRepository;
import com.ft.back.account.domain.Account;
import com.ft.back.common.exception.CustomException;
import com.ft.back.transaction.application.dto.CreateTransactionCommand;
import com.ft.back.transaction.application.dto.TransactionResult;
import com.ft.back.transaction.application.dto.UpdateTransactionCommand;
import com.ft.back.transaction.application.event.TransactionCreatedEvent;
import com.ft.back.transaction.application.port.CategoryRepository;
import com.ft.back.transaction.application.port.TransactionRepository;
import com.ft.back.transaction.domain.Category;
import com.ft.back.transaction.domain.Transaction;
import com.ft.back.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.ft.back.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

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
                command.toAccountId(),
                command.categoryId(),
                command.type(),
                command.amount(),
                command.description(),
                command.transactionDate()
        );

        updateAccountBalance(userId, account, command);

        TransactionResult result = TransactionResult.from(transactionRepository.save(transaction));

        eventPublisher.publishEvent(new TransactionCreatedEvent(
                userId,
                command.categoryId(),
                command.type(),
                command.amount(),
                command.transactionDate()
        ));

        return result;
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
    public TransactionResult update(Long userId, Long transactionId, UpdateTransactionCommand command) {
        Transaction transaction = getTransaction(transactionId);
        transaction.validateOwner(userId);

        if (command.description() != null) {
            transaction.updateDescription(command.description());
        }
        if (command.transactionDate() != null) {
            transaction.updateTransactionDate(command.transactionDate());
        }
        if (command.amount() != null) {
            BigDecimal oldAmount = transaction.getAmount();
            transaction.updateAmount(command.amount());
            reconcileBalance(userId, transaction, oldAmount, command.amount());
        }

        return TransactionResult.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Transaction transaction = getTransaction(transactionId);
        transaction.validateOwner(userId);

        restoreAccountBalance(userId, transaction);

        transactionRepository.delete(transaction);
    }

    // 잔액 반영 (거래 생성 시)
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

    // 잔액 복구 (거래 삭제 시)
    private void restoreAccountBalance(Long userId, Transaction transaction) {
        Account account = getAccount(transaction.getAccountId());
        if (transaction.getType() == TransactionType.INCOME) {
            account.withdraw(transaction.getAmount());
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            account.deposit(transaction.getAmount());
        } else if (transaction.getType() == TransactionType.TRANSFER) {
            account.deposit(transaction.getAmount());
            if (transaction.getToAccountId() != null) {
                Account toAccount = getAccount(transaction.getToAccountId());
                toAccount.validateOwner(userId);
                toAccount.withdraw(transaction.getAmount());
            }
        }
    }

    // 잔액 재조정 (금액 수정 시)
    private void reconcileBalance(Long userId, Transaction transaction, BigDecimal oldAmount, BigDecimal newAmount) {
        BigDecimal diff = newAmount.subtract(oldAmount);
        if (diff.compareTo(BigDecimal.ZERO) == 0) return;

        Account account = getAccount(transaction.getAccountId());
        account.validateOwner(userId);

        if (transaction.getType() == TransactionType.INCOME) {
            if (diff.compareTo(BigDecimal.ZERO) > 0) account.deposit(diff);
            else account.withdraw(diff.abs());
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            if (diff.compareTo(BigDecimal.ZERO) > 0) account.withdraw(diff);
            else account.deposit(diff.abs());
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
