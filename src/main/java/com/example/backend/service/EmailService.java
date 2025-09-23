package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageService messageService;

    @Value("${app.mail.from:noreply@finance-management.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendWalletShareInvitation(User owner, User sharedWithUser, Wallet wallet, String token, String customMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(sharedWithUser.getEmail());
            message.setSubject(owner.getFirstName() + " đã mời bạn cùng quản lý ví: " + wallet.getName());

            String emailContent = buildWalletShareInvitationEmailContent(owner, sharedWithUser, wallet, token, customMessage);
            message.setText(emailContent);

            mailSender.send(message);
            log.info("Email mời chia sẻ ví đã được gửi đến: {}", sharedWithUser.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email mời chia sẻ ví: {}", e.getMessage(), e);
        }
    }

    private String buildWalletShareInvitationEmailContent(User owner, User sharedWithUser, Wallet wallet, String token, String customMessage) {
        String invitationLink = frontendUrl + "/accept-invitation?token=" + token;

        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(sharedWithUser.getFirstName()).append(",\n\n");

        content.append(owner.getFirstName()).append(" ").append(owner.getLastName())
                .append(" đã mời bạn cùng tham gia quản lý ví '").append(wallet.getName()).append("'.\n\n");

        if (customMessage != null && !customMessage.isEmpty()) {
            content.append("Lời nhắn từ ").append(owner.getFirstName()).append(":\n\"");
            content.append(customMessage).append("\"\n\n");
        }

        content.append("Vui lòng nhấp vào liên kết bên dưới để xem chi tiết và chấp nhận lời mời:\n");
        content.append(invitationLink).append("\n\n");
        content.append("Lưu ý: Lời mời này sẽ hết hạn sau 48 giờ.\n\n");
        content.append("Nếu bạn không quen biết người gửi hoặc không muốn nhận lời mời này, vui lòng bỏ qua email.\n\n");

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

    public void sendEmailWithAttachment(String toEmail,
                                        String subject,
                                        String textBody,
                                        byte[] attachmentBytes,
                                        String attachmentFilename,
                                        String contentType) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(textBody, false);

            if (attachmentBytes != null && attachmentBytes.length > 0 && attachmentFilename != null) {
                helper.addAttachment(attachmentFilename, new org.springframework.core.io.ByteArrayResource(attachmentBytes) {
                    @Override
                    public String getFilename() {
                        return attachmentFilename;
                    }
                }, contentType != null ? contentType : "application/octet-stream");
            }

            mailSender.send(mimeMessage);
            log.info("Email với tệp đính kèm đã được gửi đến: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email kèm tệp đính kèm: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email với tệp đính kèm", e);
        }
    }
}