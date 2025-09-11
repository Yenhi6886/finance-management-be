package com.example.backend.controller;

import com.example.backend.entity.Wallet;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletRepository walletRepository;

    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<Wallet> wallets = walletRepository.findByUserIdAndIsArchivedFalse(userId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Wallet wallet = walletRepository.findByIdAndUserAccess(id, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc bạn không có quyền truy cập"));
        return ResponseEntity.ok(wallet);
    }

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet wallet, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        wallet.setUserId(userId);
        wallet.setIsArchived(false);
        Wallet savedWallet = walletRepository.save(wallet);
        return ResponseEntity.ok(savedWallet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wallet> updateWallet(@PathVariable Long id, @RequestBody Wallet wallet, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Wallet existingWallet = walletRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc bạn không có quyền chỉnh sửa"));
        
        existingWallet.setName(wallet.getName());
        existingWallet.setIcon(wallet.getIcon());
        existingWallet.setDescription(wallet.getDescription());
        
        Wallet updatedWallet = walletRepository.save(existingWallet);
        return ResponseEntity.ok(updatedWallet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Wallet wallet = walletRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc bạn không có quyền xóa"));
        
        walletRepository.delete(wallet);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Wallet> archiveWallet(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Wallet wallet = walletRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc bạn không có quyền thao tác"));
        
        wallet.setIsArchived(true);
        Wallet archivedWallet = walletRepository.save(wallet);
        return ResponseEntity.ok(archivedWallet);
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<Wallet> unarchiveWallet(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Wallet wallet = walletRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc bạn không có quyền thao tác"));
        
        wallet.setIsArchived(false);
        Wallet unarchivedWallet = walletRepository.save(wallet);
        return ResponseEntity.ok(unarchivedWallet);
    }

    private Long getCurrentUserId(Authentication authentication) {
        // Lấy ID người dùng từ thông tin xác thực
        // Cần điều chỉnh theo cách triển khai xác thực của bạn
        return Long.valueOf(authentication.getName());
    }
}
