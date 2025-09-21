package com.example.backend.controller;

import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ShareWalletResponse;
import com.example.backend.dto.response.SharedWalletResponse;
import com.example.backend.entity.WalletShare;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.WalletShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet-shares")
@RequiredArgsConstructor
public class WalletShareController {

    private final WalletShareService walletShareService;

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<ShareWalletResponse>> shareWalletByInvitation(
            @Valid @RequestBody ShareWalletRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ShareWalletResponse response = walletShareService.shareWallet(request, currentUser.getId());
        ApiResponse<ShareWalletResponse> apiResponse = new ApiResponse<>(
                true,
                "Lời mời chia sẻ ví đã được gửi thành công.",
                response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<ShareWalletResponse>> verifyInvitation(@RequestParam String token) {
        ShareWalletResponse response = walletShareService.verifyInvitation(token);
        ApiResponse<ShareWalletResponse> apiResponse = new ApiResponse<>(
                true,
                "Lấy thông tin lời mời thành công.",
                response
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletShareService.acceptInvitation(token, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true,
                "Chấp nhận lời mời chia sẻ ví thành công!",
                null
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletShareService.rejectInvitation(token, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true,
                "Bạn đã từ chối lời mời chia sẻ ví.",
                null
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<ApiResponse<List<SharedWalletResponse>>> getSharedWallets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<SharedWalletResponse> sharedWallets = walletShareService.getSharedWallets(currentUser.getId());
        ApiResponse<List<SharedWalletResponse>> apiResponse = new ApiResponse<>(
                true,
                "Lấy danh sách ví được chia sẻ thành công",
                sharedWallets
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/shared-by-me")
    public ResponseEntity<ApiResponse<List<ShareWalletResponse>>> getWalletsSharedByMe(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<ShareWalletResponse> sharedWallets = walletShareService.getWalletsSharedByMe(currentUser.getId());
        ApiResponse<List<ShareWalletResponse>> apiResponse = new ApiResponse<>(
                true,
                "Lấy danh sách ví đã chia sẻ và các lời mời đã gửi thành công",
                sharedWallets
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<ApiResponse<Void>> revokeWalletShareById(
            @PathVariable Long shareId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletShareService.revokeWalletShareById(shareId, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true,
                "Thu hồi/xóa chia sẻ ví thành công",
                null
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{shareId}/permission")
    public ResponseEntity<ApiResponse<Void>> updateWalletSharePermission(
            @PathVariable Long shareId,
            @RequestParam WalletShare.PermissionLevel permission,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletShareService.updateWalletSharePermission(shareId, currentUser.getId(), permission);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true,
                "Cập nhật quyền truy cập ví thành công",
                null
        );
        return ResponseEntity.ok(apiResponse);
    }
}