package com.example.backend.controller;

import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        UserResponse userResponse = userService.getCurrentUserProfile();
        ApiResponse<UserResponse> response = new ApiResponse<>(true, "Lấy thông tin profile thành công!", userResponse);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse userResponse = userService.updateProfile(request);
        ApiResponse<UserResponse> response = new ApiResponse<>(true, "Cập nhật profile thành công!", userResponse);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(@RequestParam("avatar") MultipartFile file) {
        UserResponse userResponse = userService.updateAvatar(file);
        ApiResponse<UserResponse> response = new ApiResponse<>(true, "Cập nhật ảnh đại diện thành công!", userResponse);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        ApiResponse<Void> response = new ApiResponse<>(true, "Đổi mật khẩu thành công!");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteAccount();
        ApiResponse<Void> response = new ApiResponse<>(true, "Xóa tài khoản thành công!");
        return ResponseEntity.ok(response);
    }
}