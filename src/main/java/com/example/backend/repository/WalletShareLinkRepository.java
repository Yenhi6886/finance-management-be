package com.example.backend.repository;

import com.example.backend.entity.WalletShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletShareLinkRepository extends JpaRepository<WalletShareLink, Long> {

    @Query("SELECT wsl FROM WalletShareLink wsl " +
            "JOIN FETCH wsl.wallet w " +
            "JOIN FETCH wsl.owner o " +
            "WHERE wsl.shareToken = :shareToken AND wsl.isActive = true")
    Optional<WalletShareLink> findByShareToken(@Param("shareToken") String shareToken);

    @Query("SELECT wsl FROM WalletShareLink wsl " +
            "JOIN FETCH wsl.wallet w " +
            "WHERE wsl.owner.id = :ownerId AND wsl.isActive = true")
    List<WalletShareLink> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT wsl FROM WalletShareLink wsl " +
            "WHERE wsl.wallet.id = :walletId AND wsl.isActive = true")
    List<WalletShareLink> findByWalletId(@Param("walletId") Long walletId);

    @Query("SELECT COUNT(wsl) FROM WalletShareLink wsl " +
            "WHERE wsl.wallet.id = :walletId AND wsl.isActive = true")
    Long countByWalletId(@Param("walletId") Long walletId);

    void deleteByWalletId(Long walletId);
}
