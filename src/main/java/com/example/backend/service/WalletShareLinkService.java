package com.example.backend.service;

import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.dto.response.WalletShareLinkResponse;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShareLink;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.WalletRepository;
import com.example.backend.repository.WalletShareLinkRepository;
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
public class WalletShareLinkService {

    private final WalletShareLinkRepository walletShareLinkRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public WalletShareLinkResponse createShareLink(ShareWalletRequest request, Long ownerId) {
        // Kiểm tra ví có tồn tại và thuộc về user không
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        if (!wallet.getUser().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền chia sẻ ví này");
        }

        // Tạo unique share token
        String shareToken = generateShareToken();

        // Tạo wallet share link
        WalletShareLink walletShareLink = new WalletShareLink();
        walletShareLink.setWallet(wallet);
        walletShareLink.setOwner(wallet.getUser());
        walletShareLink.setShareToken(shareToken);
        walletShareLink.setPermissionLevel(request.getPermissionLevel());
        walletShareLink.setIsActive(true);
        
        // Set expiry time if provided
        if (request.getExpiryDate() != null) {
            walletShareLink.setExpiryDate(request.getExpiryDate());
        }

        WalletShareLink savedWalletShareLink = walletShareLinkRepository.save(walletShareLink);

        log.info("Link chia sẻ ví '{}' đã được tạo bởi user '{}' với token '{}'",
                wallet.getName(), wallet.getUser().getEmail(), shareToken);

        return buildWalletShareLinkResponse(savedWalletShareLink);
    }

    public WalletShareLinkResponse getShareLinkInfo(String shareToken) {
        WalletShareLink walletShareLink = walletShareLinkRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy link chia sẻ"));

        // Kiểm tra link có hết hạn không
        if (walletShareLink.getExpiryDate() != null && walletShareLink.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Link chia sẻ đã hết hạn");
        }

        // Kiểm tra link có active không
        if (!walletShareLink.getIsActive()) {
            throw new BadRequestException("Link chia sẻ đã bị thu hồi");
        }

        return buildWalletShareLinkResponse(walletShareLink);
    }

    public List<WalletShareLinkResponse> getShareLinksByOwner(Long ownerId) {
        List<WalletShareLink> walletShareLinks = walletShareLinkRepository.findByOwnerId(ownerId);
        return walletShareLinks.stream()
                .map(this::buildWalletShareLinkResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeShareLink(Long shareLinkId, Long ownerId) {
        WalletShareLink walletShareLink = walletShareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy link chia sẻ với ID: " + shareLinkId));

        if (!walletShareLink.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Bạn không có quyền thu hồi link chia sẻ này");
        }

        walletShareLink.setIsActive(false);
        walletShareLinkRepository.save(walletShareLink);

        log.info("Link chia sẻ ví '{}' đã được thu hồi bởi user '{}'",
                walletShareLink.getWallet().getName(), ownerId);
    }

    private String generateShareToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private WalletShareLinkResponse buildWalletShareLinkResponse(WalletShareLink walletShareLink) {
        Wallet wallet = walletShareLink.getWallet();
        return WalletShareLinkResponse.builder()
                .id(walletShareLink.getId())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .ownerName(walletShareLink.getOwner().getFirstName() + " " + walletShareLink.getOwner().getLastName())
                .shareToken(walletShareLink.getShareToken())
                .permissionLevel(walletShareLink.getPermissionLevel())
                .expiryDate(walletShareLink.getExpiryDate())
                .isActive(walletShareLink.getIsActive())
                .createdAt(walletShareLink.getCreatedAt())
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
}
