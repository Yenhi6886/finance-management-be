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
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return ResponseEntity
                    .ok(ApiResponse.success("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestParam String token) {
        try {
            userService.activateAccount(token);
            return ResponseEntity.ok(ApiResponse.success("Kích hoạt tài khoản thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Link đặt lại mật khẩu đã được gửi đến email của bạn."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                var exp = jwtUtil.getExpirationDateFromToken(token);
                jwtBlacklistService.blacklistToken(token, exp.getTime());
            }
            return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}