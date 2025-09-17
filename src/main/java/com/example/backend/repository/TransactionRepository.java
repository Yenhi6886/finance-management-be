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

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = 'TRANSFER' ORDER BY t.date DESC")
    List<Transaction> findTransferTransactionsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT date(t.date) as transaction_date, " +
            "(SELECT t2.balance_after_transaction FROM transactions t2 WHERE t2.wallet_id = :walletId AND date(t2.date) <= date(t2.date) ORDER BY t2.date DESC, t2.id DESC LIMIT 1) as closing_balance " +
            "FROM transactions t " +
            "WHERE t.wallet_id = :walletId AND t.date >= :startDate " +
            "GROUP BY transaction_date " +
            "ORDER BY transaction_date ASC", nativeQuery = true)
    List<Object[]> findClosingBalanceByDate(@Param("walletId") Long walletId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpensesByCategoryIdAndDateRange(@Param("categoryId") Long categoryId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId AND t.type = 'INCOME' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumIncomesByCategoryIdAndDateRange(@Param("categoryId") Long categoryId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Query("UPDATE Transaction t SET t.category = null WHERE t.category.id = :categoryId")
    void setCategoryToNullByCategoryId(@Param("categoryId") Long categoryId);

    List<Transaction> findByCategoryIdOrderByDateDesc(Long categoryId);
}