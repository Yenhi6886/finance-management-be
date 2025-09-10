package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    // Thêm một thuộc tính để định nghĩa URL của frontend
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendActivationEmail(String toEmail, String activationToken) {
        // **THAY ĐỔI QUAN TRỌNG: Link trỏ về trang /activate của Frontend**
        String link = frontendUrl + "/activate?token=" + activationToken;

        if (!mailEnabled) {
            logger.info("[MAIL DISABLED] To enable mail sending, set 'app.mail.enabled=true' in application.properties");
            logger.info("[MAIL DEBUG] Activation link for {}: {}", toEmail, link);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Chào mừng! Vui lòng kích hoạt tài khoản của bạn");

            String text = "Cảm ơn bạn đã đăng ký.\n\n" +
                    "Vui lòng nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn:\n" +
                    link + "\n\n" +
                    "Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.";

            message.setText(text);
            mailSender.send(message);
            logger.info("Activation email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send activation email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        String link = frontendUrl + "/reset-password?token=" + resetToken;

        if (!mailEnabled) {
            logger.info("[MAIL DISABLED] To enable mail sending, set 'app.mail.enabled=true' in application.properties");
            logger.info("[MAIL DEBUG] Reset password link for {}: {}", toEmail, link);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Yêu cầu đặt lại mật khẩu");

            String text = "Bạn đã nhận được email này vì bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
                    "Vui lòng nhấp vào liên kết sau hoặc dán vào trình duyệt của bạn để hoàn tất quá trình:\n" +
                    link + "\n\n" +
                    "Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này và mật khẩu của bạn sẽ không thay đổi.";

            message.setText(text);
            mailSender.send(message);
            logger.info("Reset password email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send reset password email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendNewPasswordEmail(String toEmail, String newPassword) {
        if (!mailEnabled) {
            logger.info("[MAIL DISABLED] To enable mail sending, set 'app.mail.enabled=true' in application.properties");
            logger.info("[MAIL DEBUG] New password for {}: {}", toEmail, newPassword);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mật khẩu mới cho tài khoản Finance Management của bạn");

            String text = "Chào bạn,\n\n" +
                    "Mật khẩu mới của bạn là: " + newPassword + "\n\n" +
                    "Vì lý do bảo mật, chúng tôi thực sự khuyên bạn nên đăng nhập và đổi lại mật khẩu này ngay lập tức.\n\n" +
                    "Trân trọng,\nĐội ngũ Finance Management";

            message.setText(text);
            mailSender.send(message);
            logger.info("New password email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send new password email to {}: {}", toEmail, e.getMessage());
        }
    }
}