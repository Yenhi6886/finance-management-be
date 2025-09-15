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

    @PostMapping
    public ResponseEntity<ApiResponse<ShareWalletResponse>> shareWallet(
            @Valid @RequestBody ShareWalletRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ShareWalletResponse response = walletShareService.shareWallet(request, currentUser.getId());
        ApiResponse<ShareWalletResponse> apiResponse = new ApiResponse<>(
                true, 
                "Chia sẻ ví thành công. Email thông báo đã được gửi đến người nhận.", 
                response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/create-link")
    public ResponseEntity<ApiResponse<ShareWalletResponse>> createShareLink(
            @Valid @RequestBody ShareWalletRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ShareWalletResponse response = walletShareService.createShareLink(request, currentUser.getId());
        ApiResponse<ShareWalletResponse> apiResponse = new ApiResponse<>(
                true, 
                "Tạo link chia sẻ thành công", 
                response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/link/{shareToken}")
    public ResponseEntity<ApiResponse<ShareWalletResponse>> getShareLinkInfo(
            @PathVariable String shareToken) {

        ShareWalletResponse response = walletShareService.getShareLinkInfo(shareToken);
        ApiResponse<ShareWalletResponse> apiResponse = new ApiResponse<>(
                true, 
                "Lấy thông tin link chia sẻ thành công", 
                response
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
                "Lấy danh sách ví đã chia sẻ thành công", 
                sharedWallets
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{walletId}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> revokeWalletShare(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletShareService.revokeWalletShare(walletId, userId, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true, 
                "Thu hồi chia sẻ ví thành công", 
                null
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
                "Thu hồi chia sẻ ví thành công", 
                null
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{walletId}/users/{userId}/permission")
    public ResponseEntity<ApiResponse<Void>> updateWalletSharePermission(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @RequestParam WalletShare.PermissionLevel permission,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletShareService.updateWalletSharePermission(walletId, userId, currentUser.getId(), permission);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true, 
                "Cập nhật quyền truy cập ví thành công", 
                null
        );
        return ResponseEntity.ok(apiResponse);
    }
}
