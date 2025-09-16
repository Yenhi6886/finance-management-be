package com.example.backend.repository;

import com.example.backend.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Calculate total by wallet and category ID
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.category.id = :categoryId")
    BigDecimal calculateTotalByWalletIdAndCategory(@Param("walletId") Long walletId, @Param("categoryId") Long categoryId);

    // Calculate total by wallet and multiple categories
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.category.id IN :categoryIds")
    BigDecimal calculateTotalByWalletIdAndCategories(@Param("walletId") Long walletId, @Param("categoryIds") List<Long> categoryIds);

    void deleteByWalletId(Long walletId);

    List<Transaction> findByWallet_User_Id(Long userId, Pageable pageable);

    // Find transactions by user and category
    @Query("SELECT t FROM Transaction t WHERE t.wallet.user.id = :userId AND t.category.id = :categoryId")
    List<Transaction> findByWallet_User_IdAndCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId, Pageable pageable);

    // Find transactions by user and multiple categories
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.category.id IN :categoryIds ORDER BY t.date DESC")
    List<Transaction> findByUserIdAndCategories(@Param("userId") Long userId, @Param("categoryIds") List<Long> categoryIds, Pageable pageable);
}