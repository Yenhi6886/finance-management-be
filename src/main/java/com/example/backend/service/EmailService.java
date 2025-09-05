package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendActivationEmail(String toEmail, String activationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Kích hoạt tài khoản");
        message.setText("Vui lòng click vào link sau để kích hoạt tài khoản: " +
                "http://localhost:8080/api/auth/activate?token=" + activationToken);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu");
        message.setText("Vui lòng click vào link sau để đặt lại mật khẩu: " +
                "http://localhost:3000/reset-password?token=" + resetToken);

        mailSender.send(message);
    }
}
