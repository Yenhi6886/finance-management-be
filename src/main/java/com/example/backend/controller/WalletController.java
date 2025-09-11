package com.example.backend.controller;

import com.example.backend.entity.Wallet;
import com.example.backend.service.WalletSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletSelectionService walletSelectionService;

    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(@AuthenticationPrincipal(expression = "id") Long userId) {
       try{
              List<Wallet> wallets = walletSelectionService.listUserWallets(userId);
              return ResponseEntity.ok(wallets);
       }catch (Exception e){
              return ResponseEntity.badRequest().build();
       }
    }
    @GetMapping("/current")
    public ResponseEntity<Map<String, Long>> getCurrentWallet(@AuthenticationPrincipal(expression = "id") Long userId) {
        try{
            Long currentWalletId = walletSelectionService.getCurrentSelectedWalletId(userId).get();
            return ResponseEntity.ok(Map.of("currentWalletId", currentWalletId));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/current/{walletId}")
    public ResponseEntity<Void> setCurrentWallet(@AuthenticationPrincipal(expression = "id") Long userId, @org.springframework.web.bind.annotation.PathVariable Long walletId) {
        try{
            walletSelectionService.setCurrentSelectedWallet(userId, walletId);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
