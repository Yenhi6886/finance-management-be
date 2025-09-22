package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.entity.Notification;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách thông báo thành công", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Map<String, Long> count = notificationService.getUnreadNotificationCount(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy số thông báo chưa đọc thành công", count));
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Đánh dấu tất cả là đã đọc thành công", null));
    }
}