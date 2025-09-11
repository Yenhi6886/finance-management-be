package com.example.backend.repository;

import com.example.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByIdAndUserId(Long id, Long userId);
    List<Wallet> findAllByUserIdAndArchivedFalse(Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.archived = false ORDER BY w.createdAt DESC")
    List<Wallet> findAllActiveWalletsByUserIdOrderByCreatedAtDesc(@Param("userId")  Long userId);
}
