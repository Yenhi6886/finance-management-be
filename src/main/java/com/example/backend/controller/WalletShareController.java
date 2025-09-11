package com.example.backend.controller;

import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.response.ShareWalletResponse;
import com.example.backend.service.WalletShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet-shares")
@RequiredArgsConstructor
public class WalletShareController {

    private final WalletShareService walletShareService;

    @PostMapping
    public ResponseEntity<ShareWalletResponse> shareWallet(
            @Valid @RequestBody ShareWalletRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        ShareWalletResponse response = walletShareService.shareWallet(userId, request);
        return ResponseEntity.ok(response);
    }
    // Lấy các ví mà người dùng hiện tại đã chia sẻ
    @GetMapping("/shared")
    public ResponseEntity<List<ShareWalletResponse>> getSharedWallets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ShareWalletResponse> shares = walletShareService.getSharedWallets(userId);
        return ResponseEntity.ok(shares);
    }
    // Lấy các ví được chia sẻ với người dùng hiện tại
    @GetMapping("/received")
    public ResponseEntity<List<ShareWalletResponse>> getReceivedShares(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ShareWalletResponse> shares = walletShareService.getReceivedShares(userId);
        return ResponseEntity.ok(shares);
    }
    // Xóa bỏ quyền chia sẻ
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> revokeShare(
            @PathVariable Long shareId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        walletShareService.revokeShare(userId, shareId);
        return ResponseEntity.ok().build();
    }
    // Lấy thông tin chia sẻ qua token
    @GetMapping("/token/{shareToken}")
    public ResponseEntity<ShareWalletResponse> getShareByToken(@PathVariable String shareToken) {
        ShareWalletResponse response = walletShareService.getShareByToken(shareToken);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId(Authentication authentication) {
        // Lấy ID người dùng từ thông tin xác thực
        // Cần điều chỉnh theo cách triển khai xác thực của bạn
        return Long.valueOf(authentication.getName());
    }
}
