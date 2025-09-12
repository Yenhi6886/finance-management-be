package com.example.backend.service;

import com.example.backend.dto.request.AssignPermissionRequest;
import com.example.backend.dto.response.PermissionResponse;
import com.example.backend.dto.response.UserWalletPermissionsResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletPermission;
import com.example.backend.entity.WalletShare;
import com.example.backend.enums.PermissionType;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.BadRequestException;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletPermissionRepository;
import com.example.backend.repository.WalletRepository;
import com.example.backend.repository.WalletShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletPermissionService {

    private final WalletPermissionRepository walletPermissionRepository;
    private final WalletShareRepository walletShareRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<PermissionResponse> assignPermissions(Long walletId, Long userId, AssignPermissionRequest request, Long ownerId) {
        // Kiểm tra quyền sở hữu ví
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền quản lý quyền truy cập ví này");
        }

        // Kiểm tra wallet share tồn tại
        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví"));

        if (!walletShare.getIsActive()) {
            throw new BadRequestException("Chia sẻ ví đã bị vô hiệu hóa");
        }

        // Xóa quyền cũ
        walletPermissionRepository.deleteByWalletShareId(walletShare.getId());

        // Tạo quyền mới
        List<WalletPermission> permissions = request.getPermissions().stream()
                .map(permissionType -> {
                    WalletPermission permission = new WalletPermission();
                    permission.setWalletShare(walletShare);
                    permission.setPermissionType(permissionType);
                    permission.setIsGranted(true);
                    permission.setGrantedBy(ownerId);
                    permission.setGrantedAt(LocalDateTime.now());
                    return permission;
                })
                .collect(Collectors.toList());

        List<WalletPermission> savedPermissions = walletPermissionRepository.saveAll(permissions);

        log.info("Đã gán {} quyền cho user {} với ví {} bởi user {}", 
                permissions.size(), userId, walletId, ownerId);

        return savedPermissions.stream()
                .map(this::buildPermissionResponse)
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getUserPermissions(Long walletId, Long userId) {
        List<WalletPermission> permissions = walletPermissionRepository.findUserPermissionsForWallet(walletId, userId);
        
        return permissions.stream()
                .map(this::buildPermissionResponse)
                .collect(Collectors.toList());
    }

    public List<UserWalletPermissionsResponse> getUserWalletPermissions(Long userId) {
        List<WalletShare> walletShares = walletShareRepository.findSharedWalletsByUserId(userId);
        
        return walletShares.stream()
                .map(ws -> {
                    List<WalletPermission> permissions = walletPermissionRepository
                            .findByWalletShareIdAndIsGrantedTrue(ws.getId());
                    
                    List<PermissionType> permissionTypes = permissions.stream()
                            .map(WalletPermission::getPermissionType)
                            .collect(Collectors.toList());
                    
                    List<String> displayNames = permissions.stream()
                            .map(p -> p.getPermissionType().getDisplayName())
                            .collect(Collectors.toList());
                    
                    return UserWalletPermissionsResponse.builder()
                            .walletId(ws.getWallet().getId())
                            .walletName(ws.getWallet().getName())
                            .ownerName(ws.getOwner().getFirstName() + " " + ws.getOwner().getLastName())
                            .ownerEmail(ws.getOwner().getEmail())
                            .permissions(permissionTypes)
                            .permissionDisplayNames(displayNames)
                            .sharedAt(ws.getCreatedAt())
                            .isActive(ws.getIsActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokePermission(Long walletId, Long userId, PermissionType permissionType, Long ownerId) {
        // Kiểm tra quyền sở hữu ví
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền quản lý quyền truy cập ví này");
        }

        // Tìm và xóa quyền
        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví"));

        WalletPermission permission = walletPermissionRepository
                .findByWalletShareIdAndPermissionType(walletShare.getId(), permissionType)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền " + permissionType.getDisplayName()));

        permission.setIsGranted(false);
        walletPermissionRepository.save(permission);

        log.info("Đã thu hồi quyền {} của user {} với ví {} bởi user {}", 
                permissionType.getDisplayName(), userId, walletId, ownerId);
    }

    public boolean hasPermission(Long walletId, Long userId, PermissionType permissionType) {
        return walletPermissionRepository.hasPermission(walletId, userId, permissionType);
    }

    @Transactional
    public void assignDefaultPermissions(WalletShare walletShare) {
        List<PermissionType> defaultPermissions = getDefaultPermissions(walletShare.getPermissionLevel());
        
        List<WalletPermission> permissions = defaultPermissions.stream()
                .map(permissionType -> {
                    WalletPermission permission = new WalletPermission();
                    permission.setWalletShare(walletShare);
                    permission.setPermissionType(permissionType);
                    permission.setIsGranted(true);
                    permission.setGrantedBy(walletShare.getOwner().getId());
                    permission.setGrantedAt(LocalDateTime.now());
                    return permission;
                })
                .collect(Collectors.toList());

        walletPermissionRepository.saveAll(permissions);
        
        log.info("Đã gán {} quyền mặc định cho wallet share {}", 
                permissions.size(), walletShare.getId());
    }

    private List<PermissionType> getDefaultPermissions(WalletShare.PermissionLevel level) {
        return switch (level) {
            case VIEW -> List.of(
                    PermissionType.VIEW_WALLET,
                    PermissionType.VIEW_BALANCE,
                    PermissionType.VIEW_TRANSACTIONS
            );
            case EDIT -> List.of(
                    PermissionType.VIEW_WALLET,
                    PermissionType.VIEW_BALANCE,
                    PermissionType.VIEW_TRANSACTIONS,
                    PermissionType.EDIT_WALLET,
                    PermissionType.ADD_TRANSACTION,
                    PermissionType.EDIT_TRANSACTION,
                    PermissionType.DELETE_TRANSACTION,
                    PermissionType.VIEW_REPORTS
            );
            case ADMIN -> List.of(
                    PermissionType.VIEW_WALLET,
                    PermissionType.VIEW_BALANCE,
                    PermissionType.VIEW_TRANSACTIONS,
                    PermissionType.EDIT_WALLET,
                    PermissionType.ADD_TRANSACTION,
                    PermissionType.EDIT_TRANSACTION,
                    PermissionType.DELETE_TRANSACTION,
                    PermissionType.MANAGE_PERMISSIONS,
                    PermissionType.SHARE_WALLET,
                    PermissionType.VIEW_REPORTS,
                    PermissionType.EXPORT_DATA
            );
        };
    }

    private PermissionResponse buildPermissionResponse(WalletPermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .walletShareId(permission.getWalletShare().getId())
                .permissionType(permission.getPermissionType())
                .permissionDisplayName(permission.getPermissionType().getDisplayName())
                .isGranted(permission.getIsGranted())
                .grantedBy(permission.getGrantedBy())
                .grantedAt(permission.getGrantedAt())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
