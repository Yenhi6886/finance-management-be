package com.example.backend.repository;

import com.example.backend.entity.Transaction;
import com.example.backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = :type")
    BigDecimal calculateTotalByWalletIdAndType(@Param("walletId") Long walletId, @Param("type") TransactionType type);

    void deleteByWalletId(Long walletId);

    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);

    List<Transaction> findByWallet_User_Id(Long userId, Pageable pageable);

    List<Transaction> findByWallet_User_IdAndType(Long userId, TransactionType type, Pageable pageable);

    List<Transaction> findByWallet_User_IdAndTypeInAndCategoryIsNotNull(Long userId, List<TransactionType> types, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = 'TRANSFER'")
    List<Transaction> findTransferTransactionsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT date(t.transaction_date) as transaction_date, " +
            "(SELECT t2.balance_after_transaction FROM transactions t2 WHERE t2.wallet_id = :walletId AND date(t2.transaction_date) <= date(t.transaction_date) ORDER BY t2.transaction_date DESC, t2.id DESC LIMIT 1) as closing_balance " +
            "FROM transactions t " +
            "WHERE t.wallet_id = :walletId AND t.transaction_date >= :startDate " +
            "GROUP BY transaction_date " +
            "ORDER BY transaction_date ASC", nativeQuery = true)
    List<Object[]> findClosingBalanceByDate(@Param("walletId") Long walletId, @Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpensesByCategoryIdAndDateRange(@Param("categoryId") Long categoryId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId AND t.type = 'INCOME' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumIncomesByCategoryIdAndDateRange(@Param("categoryId") Long categoryId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Modifying
    @Query("UPDATE Transaction t SET t.category = null WHERE t.category.id = :categoryId")
    void setCategoryToNullByCategoryId(@Param("categoryId") Long categoryId);

    List<Transaction> findByCategoryIdOrderByDateDesc(Long categoryId);

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.user.id = :userId
      AND (:walletId IS NULL OR t.wallet.id = :walletId)
      AND (:startDate IS NULL OR t.date >= :startDate)
      AND (:endDate IS NULL OR t.date <= :endDate)
    ORDER BY t.date DESC
    """)
    Page<Transaction> getTransactionStatistics(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    Page<Transaction> findAllByUserIdAndDateBetween(Long userId, Instant startDate, Instant endDate, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
    SELECT COALESCE(SUM(CASE WHEN t.type = com.example.backend.enums.TransactionType.INCOME THEN t.amount WHEN t.type = com.example.backend.enums.TransactionType.EXPENSE THEN -t.amount ELSE 0 END), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND (:walletId IS NULL OR t.wallet.id = :walletId)
      AND (:startDate IS NULL OR t.date >= :startDate)
      AND (:endDate IS NULL OR t.date <= :endDate)
    """)
    BigDecimal sumAmountForStatistics(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    List<Transaction> findByCategoryIdOrderByDateDescIdDesc(Long categoryId);

    List<Transaction> findByWallet_User_IdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    List<Transaction> findByUser_IdAndDateBetweenOrderByDateDescIdDesc(Long userId, Instant startOfDay, Instant endOfDay, Pageable pageable);

    List<Transaction> findByUser_IdAndCategoryIdAndDateBetweenOrderByDateDescIdDesc(Long userId, Long categoryId, Instant startOfDay, Instant endOfDay, Pageable pageable);

}