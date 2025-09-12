package com.example.backend.controller;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.service.JwtBlacklistService;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        ApiResponse<Void> response = new ApiResponse<>(true, "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
        ApiResponse<AuthResponse> response = new ApiResponse<>(true, "Đăng nhập thành công!", authResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@RequestParam String token) {
        userService.activateAccount(token);
        ApiResponse<Void> response = new ApiResponse<>(true, "Kích hoạt tài khoản thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.getEmail());
        ApiResponse<Void> response = new ApiResponse<>(true, "Nếu email của bạn tồn tại trong hệ thống, một liên kết để đặt lại mật khẩu đã được gửi đến.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Void>> validateResetToken(@RequestParam String token) {
        userService.validatePasswordResetToken(token);
        ApiResponse<Void> response = new ApiResponse<>(true, "Token hợp lệ.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        ApiResponse<Void> response = new ApiResponse<>(true, "Mật khẩu đã được thay đổi thành công.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            var exp = jwtUtil.getExpirationDateFromToken(token);
            jwtBlacklistService.blacklistToken(token, exp.getTime());
        }
        ApiResponse<Void> response = new ApiResponse<>(true, "Đăng xuất thành công!");
        return ResponseEntity.ok(response);
    }
}