package com.ft.back.transaction.infrastructure.persistence;

import com.ft.back.transaction.application.port.TransactionRepository;
import com.ft.back.transaction.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return jpaTransactionRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return jpaTransactionRepository.findById(id);
    }

    @Override
    public List<Transaction> findAllByUserId(Long userId) {
        return jpaTransactionRepository.findAllByUserId(userId);
    }

    @Override
    public List<Transaction> findAllByUserIdAndAccountId(Long userId, Long accountId) {
        return jpaTransactionRepository.findAllByUserIdAndAccountId(userId, accountId);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaTransactionRepository.delete(transaction);
    }
}
