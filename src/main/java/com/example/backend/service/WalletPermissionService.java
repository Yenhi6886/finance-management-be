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
import com.example.backend.repository.WalletPermissionRepository;
import com.example.backend.repository.WalletShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletPermissionService {

    private final WalletPermissionRepository walletPermissionRepository;
    private final WalletShareRepository walletShareRepository;
    private final WalletService walletService;


    @Transactional
    public List<PermissionResponse> assignPermissions(Long walletId, Long userId, AssignPermissionRequest request, Long ownerId) {
        if (!walletService.isWalletOwner(walletId, ownerId)) {
            throw new BadRequestException("Bạn không có quyền quản lý quyền truy cập ví này");
        }

        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví cho người dùng này"));

        if (!walletShare.getIsActive()) {
            throw new BadRequestException("Chia sẻ ví đã bị vô hiệu hóa");
        }

        walletPermissionRepository.deleteByWalletShareId(walletShare.getId());

        List<WalletPermission> permissions = request.getPermissions().stream()
                .map(permissionType -> WalletPermission.builder()
                        .walletShare(walletShare)
                        .permissionType(permissionType)
                        .isGranted(true)
                        .grantedBy(ownerId)
                        .grantedAt(LocalDateTime.now())
                        .build())
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

                    User owner = ws.getOwner();
                    return UserWalletPermissionsResponse.builder()
                            .walletId(ws.getWallet().getId())
                            .walletName(ws.getWallet().getName())
                            .ownerName(owner != null ? owner.getFirstName() + " " + owner.getLastName() : "Không rõ")
                            .ownerEmail(owner != null ? owner.getEmail() : "Không rõ")
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
        if (!walletService.isWalletOwner(walletId, ownerId)) {
            throw new BadRequestException("Bạn không có quyền quản lý quyền truy cập ví này");
        }

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
        if (walletService.isWalletOwner(walletId, userId)) {
            return true;
        }
        return walletPermissionRepository.hasPermission(walletId, userId, permissionType);
    }

    @Transactional
    public void deleteAllPermissionsByWalletShareId(Long walletShareId) {
        walletPermissionRepository.deleteByWalletShareId(walletShareId);
        log.info("Đã xóa tất cả quyền cho wallet share ID: {}", walletShareId);
    }

    @Transactional
    public void assignDefaultPermissions(WalletShare walletShare) {
        List<PermissionType> defaultPermissions = getDefaultPermissions(walletShare.getPermissionLevel());

        List<WalletPermission> permissions = defaultPermissions.stream()
                .map(permissionType -> WalletPermission.builder()
                        .walletShare(walletShare)
                        .permissionType(permissionType)
                        .isGranted(true)
                        .grantedBy(walletShare.getOwner().getId())
                        .grantedAt(LocalDateTime.now())
                        .build())
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
            case OWNER -> Arrays.asList(PermissionType.values());
            default -> Collections.emptyList();
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