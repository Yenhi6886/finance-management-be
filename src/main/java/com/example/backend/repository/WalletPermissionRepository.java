package com.example.backend.repository;

import com.example.backend.entity.WalletPermission;
import com.example.backend.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletPermissionRepository extends JpaRepository<WalletPermission, Long> {

    List<WalletPermission> findByWalletShareId(Long walletShareId);

    boolean existsByWalletShare_SharedWithUser_IdAndWalletShare_Wallet_IdAndPermissionTypeAndIsGrantedTrue(Long userId, Long walletId, PermissionType permissionType);

    @Query("SELECT wp FROM WalletPermission wp " +
            "WHERE wp.walletShare.id = :walletShareId " +
            "AND wp.isGranted = true")
    List<WalletPermission> findByWalletShareIdAndIsGrantedTrue(@Param("walletShareId") Long walletShareId);

    @Query("SELECT wp FROM WalletPermission wp " +
            "WHERE wp.walletShare.id = :walletShareId " +
            "AND wp.permissionType = :permissionType " +
            "AND wp.isGranted = true")
    Optional<WalletPermission> findByWalletShareIdAndPermissionType(@Param("walletShareId") Long walletShareId,
                                                                    @Param("permissionType") PermissionType permissionType);

    @Query("SELECT COUNT(wp) > 0 FROM WalletPermission wp " +
            "JOIN wp.walletShare ws " +
            "WHERE ws.wallet.id = :walletId " +
            "AND ws.sharedWithUser.id = :userId " +
            "AND wp.permissionType = :permissionType " +
            "AND wp.isGranted = true " +
            "AND ws.status = com.example.backend.enums.InvitationStatus.ACCEPTED")
    boolean hasPermission(@Param("walletId") Long walletId,
                          @Param("userId") Long userId,
                          @Param("permissionType") PermissionType permissionType);

    @Query("SELECT wp FROM WalletPermission wp " +
            "JOIN wp.walletShare ws " +
            "WHERE ws.wallet.id = :walletId " +
            "AND ws.sharedWithUser.id = :userId " +
            "AND wp.isGranted = true " +
            "AND ws.status = com.example.backend.enums.InvitationStatus.ACCEPTED")
    List<WalletPermission> findUserPermissionsForWallet(@Param("walletId") Long walletId,
                                                        @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WalletPermission wp WHERE wp.walletShare.id = :walletShareId")
    void deleteByWalletShareId(@Param("walletShareId") Long walletShareId);

    @Query("SELECT COUNT(wp) FROM WalletPermission wp " +
            "WHERE wp.walletShare.id = :walletShareId " +
            "AND wp.isGranted = true")
    Long countByWalletShareIdAndIsGrantedTrue(@Param("walletShareId") Long walletShareId);
}