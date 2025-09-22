package com.example.backend.controller;

import com.example.backend.dto.request.UpdateSettingsRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.entity.UserSettings;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettings>> getUserSettings(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        UserSettings settings = settingsService.getUserSettings(currentUser.getId());
        ApiResponse<UserSettings> response = new ApiResponse<>(true, "Lấy cài đặt người dùng thành công", settings);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserSettings>> updateUserSettings(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateSettingsRequest request) {
        UserSettings updatedSettings = settingsService.updateUserSettings(currentUser.getId(), request);
        ApiResponse<UserSettings> response = new ApiResponse<>(true, "Cập nhật cài đặt thành công", updatedSettings);
        return ResponseEntity.ok(response);
    }
}