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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Validates and parses the expiry date string
     * @param expiryDateString The date string to validate
     * @return LocalDateTime if valid, null if empty, throws exception if invalid
     */
    private LocalDateTime validateAndParseExpiryDate(String expiryDateString) {
        if (expiryDateString == null || expiryDateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try to parse as ISO Local DateTime first
            LocalDateTime parsedDate = LocalDateTime.parse(expiryDateString, ISO_DATE_TIME_FORMATTER);
            
            // Check if the date is in the future
            if (parsedDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Ngày hết hạn phải là ngày trong tương lai");
            }
            
            return parsedDate;
        } catch (DateTimeParseException e) {
            // If parsing fails, try to parse as ISO string with timezone
            try {
                LocalDateTime parsedDate = LocalDateTime.parse(expiryDateString.replace("Z", ""));
                
                // Check if the date is in the future
                if (parsedDate.isBefore(LocalDateTime.now())) {
                    throw new BadRequestException("Ngày hết hạn phải là ngày trong tương lai");
                }
                
                return parsedDate;
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("Định dạng ngày không hợp lệ. Vui lòng chọn lại ngày hết hạn.");
            }
        }
    }

    @Transactional
    public ShareWalletResponse shareWallet(ShareWalletRequest request, Long ownerId) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền chia sẻ ví này");
        }

        User sharedWithUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));

        if (sharedWithUser.getId().equals(ownerId)) {
            throw new BadRequestException("Không thể chia sẻ ví cho chính mình");
        }

        if (walletShareRepository.findByWalletIdAndEmail(request.getWalletId(), request.getEmail()).isPresent()) {
            throw new BadRequestException("Ví này đã được chia sẻ với tài khoản này rồi");
        }

        WalletShare walletShare = new WalletShare();
        walletShare.setWallet(wallet);
        walletShare.setOwner(wallet.getUser());
        walletShare.setSharedWithUser(sharedWithUser);
        walletShare.setPermissionLevel(request.getPermissionLevel());
        walletShare.setIsActive(true);

        WalletShare savedWalletShare = walletShareRepository.save(walletShare);

        walletPermissionService.assignDefaultPermissions(savedWalletShare);

        emailService.sendWalletShareNotification(savedWalletShare, request.getMessage());

        log.info("Ví '{}' đã được chia sẻ với user '{}' bởi user '{}'",
                wallet.getName(), sharedWithUser.getEmail(), wallet.getUser().getEmail());

        return buildShareWalletResponse(savedWalletShare);
    }

    @Transactional
    public ShareWalletResponse createShareLink(ShareWalletRequest request, Long ownerId) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền chia sẻ ví này");
        }

        String shareToken = generateShareToken();

        WalletShare walletShare = new WalletShare();
        walletShare.setWallet(wallet);
        walletShare.setOwner(wallet.getUser());
        walletShare.setSharedWithUser(null);
        walletShare.setPermissionLevel(request.getPermissionLevel());
        walletShare.setIsActive(true);
        walletShare.setShareToken(shareToken);
        walletShare.setMessage(request.getMessage());

        // Validate and set expiry date
        if (request.getExpiryDate() != null && !request.getExpiryDate().trim().isEmpty()) {
            LocalDateTime validatedExpiryDate = validateAndParseExpiryDate(request.getExpiryDate());
            walletShare.setExpiresAt(validatedExpiryDate);
        }

        WalletShare savedWalletShare = walletShareRepository.save(walletShare);

        walletPermissionService.assignDefaultPermissions(savedWalletShare);

        log.info("Link chia sẻ ví '{}' đã được tạo bởi user '{}' với token '{}'",
                wallet.getName(), wallet.getUser().getEmail(), shareToken);

        return buildShareWalletResponse(savedWalletShare);
    }

    public ShareWalletResponse getShareLinkInfo(String shareToken) {
        WalletShare walletShare = walletShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy link chia sẻ"));

        if (walletShare.getExpiresAt() != null && walletShare.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Link chia sẻ đã hết hạn");
        }

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

        walletPermissionService.deleteAllPermissionsByWalletShareId(walletShare.getId());

        walletShare.setPermissionLevel(newPermission);
        WalletShare updatedWalletShare = walletShareRepository.save(walletShare);

        walletPermissionService.assignDefaultPermissions(updatedWalletShare);

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

        walletShareRepository.delete(walletShare);

        log.info("Chia sẻ ví '{}' (ID: {}) đã được thu hồi bởi user '{}'",
                walletShare.getWallet().getName(), shareId, ownerId);
    }

    @Transactional
    public void removeSharedUser(Long walletId, Long userId, Long ownerId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền quản lý ví này");
        }

        WalletShare walletShare = walletShareRepository.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví với user này"));

        if (!walletShare.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền xóa chia sẻ ví này");
        }

        walletPermissionService.deleteAllPermissionsByWalletShareId(walletShare.getId());

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

        if (walletShare.getSharedWithUser() != null) {
            builder.sharedWithUserId(walletShare.getSharedWithUser().getId())
                    .sharedWithEmail(walletShare.getSharedWithUser().getEmail())
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