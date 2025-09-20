package com.example.backend.repository;

import com.example.backend.dto.response.CategorySpendingResponse;
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

    List<Transaction> findByWalletIdAndDateAfterOrderByDateAsc(Long walletId, Instant startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpensesByCategoryIdAndDateRange(@Param("categoryId") Long categoryId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId AND t.type = 'INCOME' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumIncomesByCategoryIdAndDateRange(@Param("categoryId") Long categoryId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Modifying
    @Query("UPDATE Transaction t SET t.category = null WHERE t.category.id = :categoryId")
    void setCategoryToNullByCategoryId(@Param("categoryId") Long categoryId);

    List<Transaction> findByCategoryIdOrderByDateDesc(Long categoryId);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startOfDay AND :endOfDay")
    Page<Transaction> findTransactionsTodayByUser(
            @Param("userId") Long userId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            Pageable pageable
    );

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

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
          AND t.date BETWEEN :startDate AND :endDate
          AND (:walletIds IS NULL OR t.wallet.id IN :walletIds)
          AND (:transactionTypes IS NULL OR t.type IN :transactionTypes)
          AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)
        ORDER BY t.date DESC
        """)
    List<Transaction> findByUserIdAndDateRangeAndFilters(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("walletIds") List<Long> walletIds,
            @Param("transactionTypes") List<TransactionType> transactionTypes,
            @Param("categoryIds") List<Long> categoryIds
    );

    List<Transaction> findByWalletIdAndDateBetween(Long walletId, Instant start, Instant end);

    List<Transaction> findByWallet_UserIdAndDateBetween(Long userId, Instant start, Instant end);

    List<Transaction> findTop5ByWalletIdOrderByDateDesc(Long walletId);

    List<Transaction> findTop5ByWallet_UserIdOrderByDateDesc(Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND (:walletId IS NULL OR t.wallet.id = :walletId) AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            @Param("type") TransactionType type,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByWalletIdAndTypeAndDateBetween(
            @Param("walletId") Long walletId,
            @Param("type") TransactionType type,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT new com.example.backend.dto.response.CategorySpendingResponse(c.name, SUM(t.amount), c.color) " +
            "FROM Transaction t JOIN t.category c " +
            "WHERE t.wallet.id = :walletId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name, c.color " +
            "ORDER BY SUM(t.amount) DESC")
    List<CategorySpendingResponse> findExpenseByWalletIdAndDateRange(
            @Param("walletId") Long walletId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

}