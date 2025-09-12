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

    // Tìm tất cả quyền của một wallet share
    @Query("SELECT wp FROM WalletPermission wp " +
           "WHERE wp.walletShare.id = :walletShareId " +
           "AND wp.isGranted = true")
    List<WalletPermission> findByWalletShareIdAndIsGrantedTrue(@Param("walletShareId") Long walletShareId);

    // Tìm quyền cụ thể của một wallet share
    @Query("SELECT wp FROM WalletPermission wp " +
           "WHERE wp.walletShare.id = :walletShareId " +
           "AND wp.permissionType = :permissionType " +
           "AND wp.isGranted = true")
    Optional<WalletPermission> findByWalletShareIdAndPermissionType(@Param("walletShareId") Long walletShareId, 
                                                                   @Param("permissionType") PermissionType permissionType);

    // Kiểm tra user có quyền cụ thể không
    @Query("SELECT COUNT(wp) > 0 FROM WalletPermission wp " +
           "JOIN wp.walletShare ws " +
           "WHERE ws.wallet.id = :walletId " +
           "AND ws.sharedWithUser.id = :userId " +
           "AND wp.permissionType = :permissionType " +
           "AND wp.isGranted = true " +
           "AND ws.isActive = true")
    boolean hasPermission(@Param("walletId") Long walletId, 
                         @Param("userId") Long userId, 
                         @Param("permissionType") PermissionType permissionType);

    // Lấy tất cả quyền của user với một ví
    @Query("SELECT wp FROM WalletPermission wp " +
           "JOIN wp.walletShare ws " +
           "WHERE ws.wallet.id = :walletId " +
           "AND ws.sharedWithUser.id = :userId " +
           "AND wp.isGranted = true " +
           "AND ws.isActive = true")
    List<WalletPermission> findUserPermissionsForWallet(@Param("walletId") Long walletId, 
                                                        @Param("userId") Long userId);

    // Xóa tất cả quyền của một wallet share
    @Modifying
    @Transactional
    @Query("DELETE FROM WalletPermission wp WHERE wp.walletShare.id = :walletShareId")
    void deleteByWalletShareId(@Param("walletShareId") Long walletShareId);

    // Đếm số quyền của một wallet share
    @Query("SELECT COUNT(wp) FROM WalletPermission wp " +
           "WHERE wp.walletShare.id = :walletShareId " +
           "AND wp.isGranted = true")
    Long countByWalletShareIdAndIsGrantedTrue(@Param("walletShareId") Long walletShareId);
}
