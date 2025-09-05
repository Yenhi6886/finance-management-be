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

    public void sendActivationEmail(String toEmail, String activationToken) {
        String link = "http://localhost:8080/api/auth/activate?token=" + activationToken;
        if (!mailEnabled) {
            logger.info("[MAIL DISABLED] Activation link for {}: {}", toEmail, link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Kích hoạt tài khoản");
        message.setText("Vui lòng click vào link sau để kích hoạt tài khoản: " + link);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        String link = "http://localhost:3000/reset-password?token=" + resetToken;
        if (!mailEnabled) {
            logger.info("[MAIL DISABLED] Reset password link for {}: {}", toEmail, link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu");
        message.setText("Vui lòng click vào link sau để đặt lại mật khẩu: " + link);

        mailSender.send(message);
    }
}
