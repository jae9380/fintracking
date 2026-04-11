package com.ft.back.transaction.infrastructure.persistence;

import com.ft.back.transaction.domain.Transaction;
import com.ft.back.transaction.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JpaTransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUserId(Long userId);
    List<Transaction> findAllByUserIdAndAccountId(Long userId, Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.categoryId = :categoryId AND t.type = :type " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    BigDecimal sumAmountByUserIdAndCategoryIdAndTypeAndYearMonth(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("year") int year,
            @Param("month") int month);
}
