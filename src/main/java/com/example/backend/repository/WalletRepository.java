package com.example.backend.repository;

import com.example.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserIdAndIsArchivedFalse(Long userId);

    List<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.id = :walletId AND (w.userId = :userId OR EXISTS (SELECT ws FROM WalletShare ws WHERE ws.walletId = w.id AND ws.sharedWithUserId = :userId AND ws.isActive = true))")
    Optional<Wallet> findByIdAndUserAccess(@Param("walletId") Long walletId, @Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId OR EXISTS (SELECT ws FROM WalletShare ws WHERE ws.walletId = w.id AND ws.sharedWithUserId = :userId AND ws.isActive = true)")
    List<Wallet> findAccessibleWallets(@Param("userId") Long userId);
}
