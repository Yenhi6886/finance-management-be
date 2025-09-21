package com.example.backend.repository;

import com.example.backend.entity.WalletShare;
import com.example.backend.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletShareRepository extends JpaRepository<WalletShare, Long> {

    // === Các phương thức cho luồng mời mới ===

    Optional<WalletShare> findByInvitationToken(String invitationToken);

    Optional<WalletShare> findByWalletIdAndSharedWithUser_EmailAndStatus(Long walletId, String email, InvitationStatus status);

    List<WalletShare> findBySharedWithUserIdAndStatus(Long userId, InvitationStatus status);

    List<WalletShare> findByOwnerId(Long ownerId);

    Optional<WalletShare> findByIdAndOwnerId(Long shareId, Long ownerId);

    // === Các phương thức được sử dụng bởi các service khác ===

    Optional<WalletShare> findByWalletIdAndSharedWithUserId(Long walletId, Long userId);

    boolean existsByWalletIdAndSharedWithUserIdAndStatus(Long walletId, Long userId, InvitationStatus status);

    @Query("SELECT COUNT(ws) FROM WalletShare ws WHERE ws.wallet.id = :walletId AND ws.status = com.example.backend.enums.InvitationStatus.ACCEPTED")
    Long countAcceptedSharesByWalletId(@Param("walletId") Long walletId);

    @Transactional
    @Modifying
    void deleteByWalletId(Long walletId);
}