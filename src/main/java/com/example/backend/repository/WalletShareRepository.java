package com.example.backend.repository;

import com.example.backend.entity.WalletShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletShareRepository extends JpaRepository<WalletShare, Long> {

    List<WalletShare> findByWalletIdAndIsActiveTrue(Long walletId);

    List<WalletShare> findBySharedWithUserIdAndIsActiveTrue(Long sharedWithUserId);

    List<WalletShare> findByWalletUserIdAndIsActiveTrue(Long walletUserId);

    @Query("SELECT ws FROM WalletShare ws WHERE ws.walletId = :walletId AND ws.sharedWithEmail = :email AND ws.isActive = true")
    Optional<WalletShare> findByWalletIdAndEmail(@Param("walletId") Long walletId, @Param("email") String email);

    @Query("SELECT ws FROM WalletShare ws WHERE ws.shareToken = :shareToken AND ws.isActive = true AND (ws.expiryDate IS NULL OR ws.expiryDate > :now)")
    Optional<WalletShare> findByShareTokenAndValid(@Param("shareToken") String shareToken, @Param("now") LocalDateTime now);

    @Query("SELECT ws FROM WalletShare ws WHERE ws.walletId = :walletId AND ws.sharedWithUserId = :userId AND ws.isActive = true")
    Optional<WalletShare> findByWalletIdAndUserId(@Param("walletId") Long walletId, @Param("userId") Long userId);

    @Query("SELECT ws FROM WalletShare ws WHERE ws.wallet.userId = :userId AND ws.isActive = true")
    List<WalletShare> findSharedByUser(@Param("userId") Long userId);
}
