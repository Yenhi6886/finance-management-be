package com.example.backend.service;

import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.response.ShareWalletResponse;
import com.example.backend.dto.response.SharedWalletResponse;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
import com.example.backend.enums.InvitationStatus;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import com.example.backend.repository.WalletShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng sở hữu ví"));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền chia sẻ ví này");
        }

        User sharedWithUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));

        if (sharedWithUser.getId().equals(ownerId)) {
            throw new BadRequestException("Không thể chia sẻ ví cho chính mình");
        }

        walletShareRepository.findByWalletIdAndSharedWithUser_EmailAndStatus(request.getWalletId(), request.getEmail(), InvitationStatus.ACCEPTED)
                .ifPresent(share -> { throw new BadRequestException("Ví này đã được chia sẻ và chấp nhận bởi người dùng này."); });

        walletShareRepository.findByWalletIdAndSharedWithUser_EmailAndStatus(request.getWalletId(), request.getEmail(), InvitationStatus.PENDING)
                .ifPresent(share -> { throw new BadRequestException("Đã có một lời mời chia sẻ ví này đang chờ xử lý đến email này."); });

        String invitationToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(48);

        WalletShare walletShare = WalletShare.builder()
                .wallet(wallet)
                .owner(owner)
                .sharedWithUser(sharedWithUser)
                .permissionLevel(request.getPermissionLevel())
                .status(InvitationStatus.PENDING)
                .invitationToken(invitationToken)
                .expiresAt(expiresAt)
                .message(request.getMessage())
                .build();

        WalletShare savedWalletShare = walletShareRepository.save(walletShare);

        emailService.sendWalletShareInvitation(owner, sharedWithUser, wallet, invitationToken, request.getMessage());

        log.info("Lời mời chia sẻ ví '{}' đã được gửi tới '{}' bởi '{}'",
                wallet.getName(), sharedWithUser.getEmail(), owner.getEmail());

        return buildShareWalletResponse(savedWalletShare);
    }

    @Transactional(readOnly = true)
    public ShareWalletResponse verifyInvitation(String token) {
        WalletShare walletShare = walletShareRepository.findByInvitationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Lời mời không hợp lệ hoặc đã hết hạn."));

        validateInvitation(walletShare);

        return buildShareWalletResponse(walletShare);
    }

    @Transactional
    public void acceptInvitation(String token, Long acceptingUserId) {
        WalletShare walletShare = walletShareRepository.findByInvitationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Lời mời không hợp lệ hoặc đã hết hạn."));

        validateInvitation(walletShare);

        if (!walletShare.getSharedWithUser().getId().equals(acceptingUserId)) {
            throw new BadRequestException("Bạn không có quyền chấp nhận lời mời này.");
        }

        walletShare.setStatus(InvitationStatus.ACCEPTED);
        walletShare.setInvitationToken(null);
        WalletShare savedWalletShare = walletShareRepository.save(walletShare);

        walletPermissionService.assignDefaultPermissions(savedWalletShare);

        log.info("User '{}' đã chấp nhận lời mời chia sẻ ví '{}'", acceptingUserId, walletShare.getWallet().getName());
    }

    @Transactional
    public void rejectInvitation(String token, Long rejectingUserId) {
        WalletShare walletShare = walletShareRepository.findByInvitationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Lời mời không hợp lệ hoặc đã hết hạn."));

        validateInvitation(walletShare);

        if (!walletShare.getSharedWithUser().getId().equals(rejectingUserId)) {
            throw new BadRequestException("Bạn không có quyền từ chối lời mời này.");
        }

        walletShare.setStatus(InvitationStatus.REJECTED);
        walletShare.setInvitationToken(null);
        walletShareRepository.save(walletShare);

        log.info("User '{}' đã từ chối lời mời chia sẻ ví '{}'", rejectingUserId, walletShare.getWallet().getName());
    }

    private void validateInvitation(WalletShare walletShare) {
        if (walletShare.getExpiresAt().isBefore(LocalDateTime.now())) {
            walletShare.setStatus(InvitationStatus.EXPIRED);
            walletShareRepository.save(walletShare);
            throw new BadRequestException("Lời mời chia sẻ đã hết hạn.");
        }
        if (walletShare.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Lời mời này đã được xử lý hoặc đã bị thu hồi.");
        }
    }

    public List<SharedWalletResponse> getSharedWallets(Long userId) {
        return walletShareRepository.findBySharedWithUserIdAndStatus(userId, InvitationStatus.ACCEPTED)
                .stream()
                .map(this::buildSharedWalletResponse)
                .collect(Collectors.toList());
    }

    public List<ShareWalletResponse> getWalletsSharedByMe(Long ownerId) {
        return walletShareRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::buildShareWalletResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeWalletShareById(Long shareId, Long ownerId) {
        WalletShare walletShare = walletShareRepository.findByIdAndOwnerId(shareId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví với ID: " + shareId + " hoặc bạn không có quyền."));

        if (walletShare.getStatus() == InvitationStatus.PENDING) {
            walletShare.setStatus(InvitationStatus.REVOKED);
            walletShareRepository.save(walletShare);
            log.info("Lời mời chia sẻ ví '{}' (ID: {}) đã được thu hồi bởi chủ sở hữu '{}'",
                    walletShare.getWallet().getName(), shareId, ownerId);
        } else if (walletShare.getStatus() == InvitationStatus.ACCEPTED) {
            walletShareRepository.delete(walletShare);
            log.info("Chia sẻ ví '{}' (ID: {}) đã được xóa hoàn toàn bởi chủ sở hữu '{}'",
                    walletShare.getWallet().getName(), shareId, ownerId);
        } else {
            walletShareRepository.delete(walletShare);
        }
    }

    @Transactional
    public void updateWalletSharePermission(Long shareId, Long ownerId, WalletShare.PermissionLevel newPermission) {
        WalletShare walletShare = walletShareRepository.findByIdAndOwnerId(shareId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví hoặc bạn không có quyền."));

        if (walletShare.getStatus() != InvitationStatus.ACCEPTED) {
            throw new BadRequestException("Chỉ có thể thay đổi quyền của các ví đã được chấp nhận chia sẻ.");
        }

        walletPermissionService.deleteAllPermissionsByWalletShareId(walletShare.getId());
        walletShare.setPermissionLevel(newPermission);
        WalletShare updatedWalletShare = walletShareRepository.save(walletShare);
        walletPermissionService.assignDefaultPermissions(updatedWalletShare);

        log.info("Quyền truy cập ví '{}' cho user '{}' đã được cập nhật thành '{}'",
                walletShare.getWallet().getName(), walletShare.getSharedWithUser().getEmail(), newPermission.getDisplayName());
    }

    private ShareWalletResponse buildShareWalletResponse(WalletShare walletShare) {
        Wallet wallet = walletShare.getWallet();
        return ShareWalletResponse.builder()
                .id(walletShare.getId())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .ownerName(walletShare.getOwner().getFirstName() + " " + walletShare.getOwner().getLastName())
                .sharedWithUserId(walletShare.getSharedWithUser().getId())
                .sharedWithEmail(walletShare.getSharedWithUser().getEmail())
                .sharedWithName(walletShare.getSharedWithUser().getFirstName() + " " + walletShare.getSharedWithUser().getLastName())
                .permissionLevel(walletShare.getPermissionLevel())
                .status(walletShare.getStatus())
                .createdAt(walletShare.getCreatedAt())
                .message(walletShare.getMessage())
                .invitationToken(walletShare.getInvitationToken())
                .expiresAt(walletShare.getExpiresAt())
                .wallet(createWalletResponse(wallet))
                .build();
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
    @Transactional
    public void removeSharedUser(Long walletId, Long userId, Long ownerId) {
        WalletShare walletShare = walletShareRepository.findByWalletIdAndSharedWithUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ ví với người dùng này"));

        if (!walletShare.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền xóa chia sẻ ví này");
        }

        // Xóa tất cả các quyền liên quan trước khi xóa bản ghi chia sẻ
        walletPermissionService.deleteAllPermissionsByWalletShareId(walletShare.getId());

        walletShareRepository.delete(walletShare);

        log.info("Đã xóa hoàn toàn chia sẻ ví '{}' với user ID '{}' bởi owner ID '{}'",
                walletShare.getWallet().getName(), userId, ownerId);
    }
}