package com.example.backend.service;

import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.response.ShareWalletResponse;
import com.example.backend.dto.response.SharedWalletResponse;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.BadRequestException;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import com.example.backend.repository.WalletShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletShareService {

    private final WalletShareRepository walletShareRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final WalletPermissionService walletPermissionService;

    @Transactional
    public ShareWalletResponse shareWallet(ShareWalletRequest request, Long ownerId) {
        // Kiểm tra ví có tồn tại và thuộc về user không
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền chia sẻ ví này");
        }

        // Kiểm tra user nhận có tồn tại không
        User sharedWithUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));

        // Kiểm tra không chia sẻ cho chính mình
        if (sharedWithUser.getId().equals(ownerId)) {
            throw new BadRequestException("Không thể chia sẻ ví cho chính mình");
        }

        // Kiểm tra ví đã được chia sẻ với user này chưa
        if (walletShareRepository.findByWalletIdAndEmail(request.getWalletId(), request.getEmail()).isPresent()) {
            throw new BadRequestException("Ví này đã được chia sẻ với tài khoản này rồi");
        }

        // Tạo wallet share
        WalletShare walletShare = new WalletShare();
        walletShare.setWallet(wallet);
        walletShare.setOwner(wallet.getUser());
        walletShare.setSharedWithUser(sharedWithUser);
        walletShare.setPermissionLevel(request.getPermissionLevel());
        walletShare.setIsActive(true);

        WalletShare savedWalletShare = walletShareRepository.save(walletShare);

        // Gán quyền mặc định
        walletPermissionService.assignDefaultPermissions(savedWalletShare);

        // Gửi email thông báo
        emailService.sendWalletShareNotification(savedWalletShare, request.getMessage());

        log.info("Ví '{}' đã được chia sẻ với user '{}' bởi user '{}'",
                wallet.getName(), sharedWithUser.getEmail(), wallet.getUser().getEmail());

        return buildShareWalletResponse(savedWalletShare);
    }

    @Transactional
    public ShareWalletResponse createShareLink(ShareWalletRequest request, Long ownerId) {
        // Kiểm tra ví có tồn tại và thuộc về user không
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền chia sẻ ví này");
        }

        // Tạo unique share token
        String shareToken = generateShareToken();

        // Tạo wallet share với share token
        WalletShare walletShare = new WalletShare();
        walletShare.setWallet(wallet);
        walletShare.setOwner(wallet.getUser());
        walletShare.setSharedWithUser(null); // Link share không cần specific user
        walletShare.setPermissionLevel(request.getPermissionLevel());
        walletShare.setIsActive(true);
        walletShare.setShareToken(shareToken);
        walletShare.setMessage(request.getMessage());
        
        // Set expiry time if provided
        if (request.getExpiryDate() != null) {
            walletShare.setExpiresAt(request.getExpiryDate());
        }

        WalletShare savedWalletShare = walletShareRepository.save(walletShare);

        // Gán quyền mặc định
        walletPermissionService.assignDefaultPermissions(savedWalletShare);

        log.info("Link chia sẻ ví '{}' đã được tạo bởi user '{}' với token '{}'",
                wallet.getName(), wallet.getUser().getEmail(), shareToken);

        return buildShareWalletResponse(savedWalletShare);
    }

    public ShareWalletResponse getShareLinkInfo(String shareToken) {
        WalletShare walletShare = walletShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy link chia sẻ"));

        // Kiểm tra link có hết hạn không
        if (walletShare.getExpiresAt() != null && walletShare.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Link chia sẻ đã hết hạn");
        }

        // Kiểm tra link có active không
        if (!walletShare.getIsActive()) {
            throw new BadRequestException("Link chia sẻ đã bị thu hồi");
        }

        return buildShareWalletResponse(walletShare);
    }

    private String generateShareToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public List<SharedWalletResponse> getSharedWallets(Long userId) {
        List<WalletShare> walletShares = walletShareRepository.findSharedWalletsByUserId(userId);

        return walletShares.stream()
                .map(this::buildSharedWalletResponse)
                .collect(Collectors.toList());
    }

    public List<ShareWalletResponse> getWalletsSharedByMe(Long ownerId) {
        List<WalletShare> walletShares = walletShareRepository.findWalletsSharedByUserId(ownerId);

        return walletShares.stream()
                .map(this::buildShareWalletResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeWalletShare(Long walletId, Long userId, Long ownerId) {
        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví"));

        if (!walletShare.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền thu hồi chia sẻ ví này");
        }

        walletShare.setIsActive(false);
        walletShareRepository.save(walletShare);

        log.info("Chia sẻ ví '{}' với user '{}' đã được thu hồi bởi user '{}'",
                walletShare.getWallet().getName(), walletShare.getSharedWithUser().getEmail(), ownerId);
    }

    @Transactional
    public void updateWalletSharePermission(Long walletId, Long userId, Long ownerId, WalletShare.PermissionLevel newPermission) {
        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví"));

        if (!walletShare.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền cập nhật quyền truy cập ví này");
        }

        walletShare.setPermissionLevel(newPermission);
        walletShareRepository.save(walletShare);

        log.info("Quyền truy cập ví '{}' với user '{}' đã được cập nhật thành '{}' bởi user '{}'",
                walletShare.getWallet().getName(), walletShare.getSharedWithUser().getEmail(),
                newPermission.getDisplayName(), ownerId);
    }

    @Transactional
    public void revokeWalletShareById(Long shareId, Long ownerId) {
        WalletShare walletShare = walletShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví với ID: " + shareId));

        if (!walletShare.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền thu hồi chia sẻ ví này");
        }

        walletShare.setIsActive(false);
        walletShareRepository.save(walletShare);

        log.info("Chia sẻ ví '{}' với user '{}' đã được thu hồi bởi user '{}'",
                walletShare.getWallet().getName(), walletShare.getSharedWithUser().getEmail(), ownerId);
    }

    @Transactional
    public void removeSharedUser(Long walletId, Long userId, Long ownerId) {
        // Kiểm tra ví có tồn tại và thuộc về user không
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền quản lý ví này");
        }

        // Tìm wallet share
        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví với user này"));

        if (!walletShare.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền xóa chia sẻ ví này");
        }

        // Xóa tất cả quyền liên quan trước
        walletPermissionService.deleteAllPermissionsByWalletShareId(walletShare.getId());

        // Xóa wallet share record
        walletShareRepository.delete(walletShare);

        log.info("Đã xóa hoàn toàn chia sẻ ví '{}' với user '{}' bởi user '{}'",
                wallet.getName(), walletShare.getSharedWithUser().getEmail(), ownerId);
    }

    private ShareWalletResponse buildShareWalletResponse(WalletShare walletShare) {
        Wallet wallet = walletShare.getWallet();
        ShareWalletResponse.ShareWalletResponseBuilder builder = ShareWalletResponse.builder()
                .id(walletShare.getId())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .ownerName(walletShare.getOwner().getFirstName() + " " + walletShare.getOwner().getLastName())
                .permissionLevel(walletShare.getPermissionLevel())
                .isActive(walletShare.getIsActive())
                .createdAt(walletShare.getCreatedAt())
                .message(walletShare.getMessage())
                .shareToken(walletShare.getShareToken())
                .expiresAt(walletShare.getExpiresAt())
                .wallet(createWalletResponse(wallet));

        // Chỉ set sharedWithUser nếu có (không phải link share)
        if (walletShare.getSharedWithUser() != null) {
            builder.sharedWithEmail(walletShare.getSharedWithUser().getEmail())
                   .sharedWithName(walletShare.getSharedWithUser().getFirstName() + " " + walletShare.getSharedWithUser().getLastName());
        }

        return builder.build();
    }

    private WalletResponse createWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setName(wallet.getName());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setIcon(wallet.getIcon());
        response.setDescription(wallet.getDescription());
        response.setArchived(wallet.isArchived());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    private SharedWalletResponse buildSharedWalletResponse(WalletShare walletShare) {
        Wallet wallet = walletShare.getWallet();
        User owner = walletShare.getOwner();

        return SharedWalletResponse.builder()
                .id(wallet.getId())
                .name(wallet.getName())
                .icon(wallet.getIcon())
                .currency(wallet.getCurrency().name())
                .balance(wallet.getBalance())
                .description(wallet.getDescription())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .ownerEmail(owner.getEmail())
                .permissionLevel(walletShare.getPermissionLevel())
                .sharedAt(walletShare.getCreatedAt())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}