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

    @GetMapping("/shared")
    public ResponseEntity<List<ShareWalletResponse>> getSharedWallets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ShareWalletResponse> shares = walletShareService.getSharedWallets(userId);
        return ResponseEntity.ok(shares);
    }

    @GetMapping("/received")
    public ResponseEntity<List<ShareWalletResponse>> getReceivedShares(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ShareWalletResponse> shares = walletShareService.getReceivedShares(userId);
        return ResponseEntity.ok(shares);
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> revokeShare(
            @PathVariable Long shareId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        walletShareService.revokeShare(userId, shareId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/token/{shareToken}")
    public ResponseEntity<ShareWalletResponse> getShareByToken(@PathVariable String shareToken) {
        ShareWalletResponse response = walletShareService.getShareByToken(shareToken);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId(Authentication authentication) {
        // 从认证信息中获取用户ID
        // 这里需要根据你的认证实现来调整
        return Long.valueOf(authentication.getName());
    }
}
