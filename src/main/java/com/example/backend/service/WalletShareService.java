package com.example.backend.service;

import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.response.ShareWalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
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

    @Transactional
    public ShareWalletResponse shareWallet(Long userId, ShareWalletRequest request) {
        // Xác minh ví có tồn tại và người dùng có quyền
        Wallet wallet = walletRepository.findByIdAndUserId(request.getWalletId(), userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc bạn không có quyền"));

        // Tạo token chia sẻ
        String shareToken = UUID.randomUUID().toString();

        // Tạo bản ghi chia sẻ
        WalletShare walletShare = new WalletShare();
        walletShare.setWalletId(request.getWalletId());
        walletShare.setShareToken(shareToken);
        walletShare.setPermissionLevel(request.getPermissionLevel());
        walletShare.setShareMethod(request.getShareMethod());
        walletShare.setMessage(request.getMessage());
        walletShare.setExpiryDate(request.getExpiryDate());
        walletShare.setIsActive(true);

        // Xử lý chia sẻ qua email
        if ("email".equals(request.getShareMethod()) && request.getRecipients() != null) {
            for (String email : request.getRecipients()) {
                // Tìm kiếm người dùng có tồn tại
                User sharedWithUser = userRepository.findByEmail(email).orElse(null);
                
                WalletShare emailShare = new WalletShare();
                emailShare.setWalletId(request.getWalletId());
                emailShare.setSharedWithEmail(email);
                emailShare.setSharedWithUserId(sharedWithUser != null ? sharedWithUser.getId() : null);
                emailShare.setShareToken(shareToken);
                emailShare.setPermissionLevel(request.getPermissionLevel());
                emailShare.setShareMethod(request.getShareMethod());
                emailShare.setMessage(request.getMessage());
                emailShare.setExpiryDate(request.getExpiryDate());
                emailShare.setIsActive(true);
                
                walletShareRepository.save(emailShare);

                // Gửi thông báo email
                if (sharedWithUser != null) {
                    sendWalletShareEmail(sharedWithUser, wallet, emailShare);
                } else {
                    sendWalletInviteEmail(email, wallet, emailShare);
                }
            }
        } else {
            // Chia sẻ qua liên kết
            walletShare.setSharedWithEmail("link");
            walletShareRepository.save(walletShare);
        }

        return buildShareResponse(walletShare, wallet);
    }

    public List<ShareWalletResponse> getSharedWallets(Long userId) {
        List<WalletShare> shares = walletShareRepository.findSharedByUser(userId);
        return shares.stream()
                .map(share -> {
                    Wallet wallet = walletRepository.findById(share.getWalletId()).orElse(null);
                    return buildShareResponse(share, wallet);
                })
                .collect(Collectors.toList());
    }

    public List<ShareWalletResponse> getReceivedShares(Long userId) {
        List<WalletShare> shares = walletShareRepository.findBySharedWithUserIdAndIsActiveTrue(userId);
        return shares.stream()
                .map(share -> {
                    Wallet wallet = walletRepository.findById(share.getWalletId()).orElse(null);
                    return buildShareResponse(share, wallet);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeShare(Long userId, Long shareId) {
        WalletShare share = walletShareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Bản ghi chia sẻ không tồn tại"));

        // Xác minh người dùng có quyền thu hồi chia sẻ
        Wallet wallet = walletRepository.findById(share.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!wallet.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thu hồi chia sẻ này");
        }

        share.setIsActive(false);
        walletShareRepository.save(share);
    }

    public ShareWalletResponse getShareByToken(String shareToken) {
        WalletShare share = walletShareRepository.findByShareTokenAndValid(shareToken, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Liên kết chia sẻ không hợp lệ hoặc đã hết hạn"));

        Wallet wallet = walletRepository.findById(share.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        return buildShareResponse(share, wallet);
    }

    private void sendWalletShareEmail(User user, Wallet wallet, WalletShare share) {
        try {
            String subject = "Thông báo chia sẻ ví - " + wallet.getName();
            String content = buildShareEmailContent(user.getFirstName(), wallet, share);
            emailService.sendEmail(user.getEmail(), subject, content);
            log.info("Email chia sẻ ví đã được gửi cho: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Gửi email chia sẻ ví thất bại: {}", e.getMessage());
        }
    }

    private void sendWalletInviteEmail(String email, Wallet wallet, WalletShare share) {
        try {
            String subject = "Lời mời ví - " + wallet.getName();
            String content = buildInviteEmailContent(email, wallet, share);
            emailService.sendEmail(email, subject, content);
            log.info("Email mời ví đã được gửi cho: {}", email);
        } catch (Exception e) {
            log.error("Gửi email mời ví thất bại: {}", e.getMessage());
        }
    }

    private String buildShareEmailContent(String firstName, Wallet wallet, WalletShare share) {
        return String.format("""
            <html>
            <body>
                <h2>Thông báo chia sẻ ví</h2>
                <p>Xin chào %s,</p>
                <p>Bạn đã nhận được một chia sẻ ví:</p>
                <ul>
                    <li><strong>Tên ví:</strong>%s</li>
                    <li><strong>Cấp độ quyền:</strong>%s</li>
                    <li><strong>Thời gian chia sẻ:</strong>%s</li>
                </ul>
                %s
                <p>Vui lòng đăng nhập vào tài khoản của bạn để xem chi tiết.</p>
                <p>Email này được gửi tự động bởi hệ thống, vui lòng không trả lời.</p>
            </body>
            </html>
            """, 
            firstName,
            wallet.getName(),
            getPermissionLevelText(share.getPermissionLevel()),
            share.getCreatedAt().toString(),
            share.getMessage() != null ? "<p><strong>Ghi chú:</strong>" + share.getMessage() + "</p>" : ""
        );
    }

    private String buildInviteEmailContent(String email, Wallet wallet, WalletShare share) {
        return String.format("""
            <html>
            <body>
                <h2>钱包邀请</h2>
                <p>您好，</p>
                <p>您被邀请访问一个钱包：</p>
                <ul>
                    <li><strong>钱包名称：</strong>%s</li>
                    <li><strong>权限级别：</strong>%s</li>
                    <li><strong>邀请时间：</strong>%s</li>
                </ul>
                %s
                <p>请注册账户后访问：<a href="%s">点击这里</a></p>
                <p>此邮件由系统自动发送，请勿回复。</p>
            </body>
            </html>
            """, 
            wallet.getName(),
            getPermissionLevelText(share.getPermissionLevel()),
            share.getCreatedAt().toString(),
            share.getMessage() != null ? "<p><strong>备注：</strong>" + share.getMessage() + "</p>" : "",
            "http://localhost:3000/register"
        );
    }

    private String getPermissionLevelText(WalletShare.PermissionLevel level) {
        return switch (level) {
            case VIEWER -> "只读权限";
            case EDITOR -> "编辑权限";
            case OWNER -> "完全权限";
        };
    }

    private ShareWalletResponse buildShareResponse(WalletShare share, Wallet wallet) {
        return ShareWalletResponse.builder()
                .id(share.getId())
                .walletId(share.getWalletId())
                .walletName(wallet != null ? wallet.getName() : "未知钱包")
                .walletIcon(wallet != null ? wallet.getIcon() : null)
                .permissionLevel(share.getPermissionLevel())
                .shareMethod(share.getShareMethod())
                .recipients(share.getSharedWithEmail() != null ? List.of(share.getSharedWithEmail()) : List.of())
                .message(share.getMessage())
                .shareToken(share.getShareToken())
                .shareLink(share.getShareToken() != null ? 
                    "http://localhost:3000/shared-wallet/" + share.getShareToken() : null)
                .expiryDate(share.getExpiryDate())
                .isActive(share.getIsActive())
                .createdAt(share.getCreatedAt())
                .build();
    }
}
