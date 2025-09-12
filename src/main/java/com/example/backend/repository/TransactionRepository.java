package com.example.backend.repository;

import com.example.backend.entity.Transaction;
import com.example.backend.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = :type")
    BigDecimal calculateTotalByWalletIdAndType(@Param("walletId") Long walletId, @Param("type") TransactionType type);

}