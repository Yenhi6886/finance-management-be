package com.example.backend.repository;

import com.example.backend.entity.WalletShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletShareRepository extends JpaRepository<WalletShare, Long> {

    @Query("SELECT ws FROM WalletShare ws " +
            "JOIN FETCH ws.wallet w " +
            "JOIN FETCH ws.owner o " +
            "WHERE ws.sharedWithUser.id = :userId AND ws.isActive = true")
    List<WalletShare> findSharedWalletsByUserId(@Param("userId") Long userId);

    @Query("SELECT ws FROM WalletShare ws " +
            "JOIN FETCH ws.wallet w " +
            "JOIN FETCH ws.sharedWithUser swu " +
            "WHERE ws.owner.id = :ownerId AND ws.isActive = true")
    List<WalletShare> findWalletsSharedByUserId(@Param("ownerId") Long ownerId);

    @Query("SELECT ws FROM WalletShare ws " +
            "WHERE ws.wallet.id = :walletId " +
            "AND ws.sharedWithUser.email = :email " +
            "AND ws.isActive = true")
    Optional<WalletShare> findByWalletIdAndEmail(@Param("walletId") Long walletId, @Param("email") String email);

    @Query("SELECT ws FROM WalletShare ws " +
            "JOIN FETCH ws.wallet w " +
            "JOIN FETCH ws.owner o " +
            "WHERE ws.wallet.id = :walletId " +
            "AND ws.sharedWithUser.id = :userId " +
            "AND ws.isActive = true")
    Optional<WalletShare> findByWalletIdAndUserId(@Param("walletId") Long walletId, @Param("userId") Long userId);

    @Query("SELECT COUNT(ws) FROM WalletShare ws " +
            "WHERE ws.wallet.id = :walletId AND ws.isActive = true")
    Long countByWalletId(@Param("walletId") Long walletId);

    void deleteByWalletId(Long walletId);
}