package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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

    @Async
    public void sendActivationEmail(String toEmail, String activationToken) {
        String link = "https://frontend-app/activate?token=" + activationToken;

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

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = "https://frontend-app/reset-password?token=" + token;
        if (!mailEnabled) {
            logger.info("[MAIL DISABLED] To enable mail sending, set 'app.mail.enabled=true' in application.properties");
            logger.info("[MAIL DEBUG] Password reset link for {}: {}", toEmail, resetUrl);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Yêu cầu đặt lại mật khẩu");
            message.setText("Để đặt lại mật khẩu của bạn, vui lòng nhấp vào liên kết dưới đây:\n" + resetUrl
                    + "\n\nLiên kết này sẽ hết hạn sau 15 phút. Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.");
            mailSender.send(message);
            logger.info("Password reset email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
