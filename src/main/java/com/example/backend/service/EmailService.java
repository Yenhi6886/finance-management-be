package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@finance-management.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendWalletShareNotification(WalletShare walletShare, String customMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(walletShare.getSharedWithUser().getEmail());
            message.setSubject("Bạn đã được chia sẻ ví: " + walletShare.getWallet().getName());

            String emailContent = buildWalletShareEmailContent(walletShare, customMessage);
            message.setText(emailContent);

            mailSender.send(message);
            log.info("Email thông báo chia sẻ ví đã được gửi đến: {}", walletShare.getSharedWithUser().getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo chia sẻ ví: {}", e.getMessage(), e);
        }
    }

    private String buildWalletShareEmailContent(WalletShare walletShare, String customMessage) {
        User owner = walletShare.getOwner();
        User sharedWithUser = walletShare.getSharedWithUser();
        Wallet wallet = walletShare.getWallet();

        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(sharedWithUser.getFirstName()).append(" ").append(sharedWithUser.getLastName()).append(",\n\n");
        
        content.append(owner.getFirstName()).append(" ").append(owner.getLastName())
               .append(" đã chia sẻ ví '").append(wallet.getName()).append("' với bạn.\n\n");
        
        content.append("Thông tin ví:\n");
        content.append("- Tên ví: ").append(wallet.getName()).append("\n");
        content.append("- Loại tiền tệ: ").append(wallet.getCurrency()).append("\n");
        content.append("- Số dư ban đầu: ").append(wallet.getInitialBalance()).append(" ").append(wallet.getCurrency()).append("\n");
        content.append("- Quyền truy cập: ").append(walletShare.getPermissionLevel().getDisplayName()).append("\n");
        
        if (wallet.getDescription() != null && !wallet.getDescription().isEmpty()) {
            content.append("- Mô tả: ").append(wallet.getDescription()).append("\n");
        }
        
        if (customMessage != null && !customMessage.isEmpty()) {
            content.append("\nLời nhắn từ ").append(owner.getFirstName()).append(":\n");
            content.append(customMessage).append("\n");
        }
        
        content.append("\nBạn có thể truy cập ví này trong ứng dụng quản lý tài chính của mình.\n");
        content.append("Đăng nhập tại: ").append(frontendUrl).append("/login\n\n");
        
        content.append("Trân trọng,\n");
        content.append("Đội ngũ Finance Management");

        return content.toString();
    }

    public void sendWalletShareAcceptedNotification(WalletShare walletShare) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(walletShare.getOwner().getEmail());
            message.setSubject("Ví của bạn đã được chấp nhận chia sẻ");

            String emailContent = buildWalletShareAcceptedEmailContent(walletShare);
            message.setText(emailContent);

            mailSender.send(message);
            log.info("Email thông báo chấp nhận chia sẻ ví đã được gửi đến: {}", walletShare.getOwner().getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo chấp nhận chia sẻ ví: {}", e.getMessage(), e);
        }
    }

    private String buildWalletShareAcceptedEmailContent(WalletShare walletShare) {
        User owner = walletShare.getOwner();
        User sharedWithUser = walletShare.getSharedWithUser();
        Wallet wallet = walletShare.getWallet();

        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(owner.getFirstName()).append(" ").append(owner.getLastName()).append(",\n\n");
        
        content.append(sharedWithUser.getFirstName()).append(" ").append(sharedWithUser.getLastName())
               .append(" đã chấp nhận chia sẻ ví '").append(wallet.getName()).append("' với bạn.\n\n");
        
        content.append("Bây giờ ").append(sharedWithUser.getFirstName())
               .append(" có thể truy cập ví này với quyền: ")
               .append(walletShare.getPermissionLevel().getDisplayName()).append("\n\n");
        
        content.append("Trân trọng,\n");
        content.append("Đội ngũ Finance Management");

        return content.toString();
    }

    public void sendActivationEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Kích hoạt tài khoản Finance Management");

            String emailContent = buildActivationEmailContent(token);
            message.setText(emailContent);

            mailSender.send(message);
            log.info("Email kích hoạt tài khoản đã được gửi đến: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email kích hoạt tài khoản: {}", e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Đặt lại mật khẩu Finance Management");

            String emailContent = buildPasswordResetEmailContent(token);
            message.setText(emailContent);

            mailSender.send(message);
            log.info("Email đặt lại mật khẩu đã được gửi đến: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu: {}", e.getMessage(), e);
        }
    }

    private String buildActivationEmailContent(String token) {
        StringBuilder content = new StringBuilder();
        content.append("Xin chào,\n\n");
        content.append("Cảm ơn bạn đã đăng ký tài khoản Finance Management!\n\n");
        content.append("Để kích hoạt tài khoản, vui lòng nhấp vào liên kết sau:\n");
        content.append(frontendUrl).append("/activate?token=").append(token).append("\n\n");
        content.append("Liên kết này sẽ hết hạn sau 1 giờ.\n\n");
        content.append("Nếu bạn không yêu cầu tạo tài khoản này, vui lòng bỏ qua email này.\n\n");
        content.append("Trân trọng,\n");
        content.append("Đội ngũ Finance Management");

        return content.toString();
    }

    private String buildPasswordResetEmailContent(String token) {
        StringBuilder content = new StringBuilder();
        content.append("Xin chào,\n\n");
        content.append("Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Finance Management.\n\n");
        content.append("Để đặt lại mật khẩu, vui lòng nhấp vào liên kết sau:\n");
        content.append(frontendUrl).append("/reset-password?token=").append(token).append("\n\n");
        content.append("Liên kết này sẽ hết hạn sau 15 phút.\n\n");
        content.append("Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n");
        content.append("Trân trọng,\n");
        content.append("Đội ngũ Finance Management");

        return content.toString();
    }
}