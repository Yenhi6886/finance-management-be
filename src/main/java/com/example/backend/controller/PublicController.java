package com.example.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.util.JwtUtil;

import io.jsonwebtoken.Claims;

@RestController
public class PublicController {

    private final JwtUtil jwtUtil;

    public PublicController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Dùng để test ngay sau khi login Google thành công
    @GetMapping("/public/oauth2-success")
    public ApiResponse oauth2Success(@CookieValue(value = "ACCESS_TOKEN", required = false) String token) {
        if (token == null) {
            return ApiResponse.error("No token found");
        }

        if (!jwtUtil.validateToken(token)) {
            return ApiResponse.error("Invalid or expired token");
        }

        Claims claims = jwtUtil.getAllClaimsFromToken(token);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", claims.getSubject());
        userData.put("name", claims.get("name"));
        userData.put("picture", claims.get("picture"));

        return ApiResponse.success("Login successful", userData);
    }

    // FE sẽ gọi endpoint này để lấy thông tin user hiện tại từ JWT
    @GetMapping("/api/me")
    public ApiResponse getCurrentUser(@CookieValue(value = "ACCESS_TOKEN", required = false) String token) {
        if (token == null) {
            return ApiResponse.error("No token found");
        }

        if (!jwtUtil.validateToken(token)) {
            return ApiResponse.error("Invalid or expired token");
        }

        Claims claims = jwtUtil.getAllClaimsFromToken(token);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", claims.getSubject());
        userData.put("name", claims.get("name"));
        userData.put("picture", claims.get("picture"));

        return ApiResponse.success("User info retrieved successfully", userData);
    }
}
