package com.example.backend.controller;

import com.example.backend.dto.WalletDto;
import com.example.backend.entity.Wallet;
import com.example.backend.mapper.WalletMapper;
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
    private final WalletMapper walletMapper;

    @GetMapping
    public ResponseEntity<List<WalletDto>> getWallets(@AuthenticationPrincipal(expression = "id") Long userId) {
       try{
              List<Wallet> wallets = walletSelectionService.listUserWallets(userId);
              List<WalletDto> walletDtos = walletMapper.toDtoList(wallets);
              return ResponseEntity.ok(walletDtos);
       }catch (Exception e){
              return ResponseEntity.badRequest().build();
       }
    }
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentWallet(@AuthenticationPrincipal(expression = "id") Long userId) {
        try{
           Optional<Long> currentWalletId = walletSelectionService.getCurrentSelectedWalletId(userId);
            return currentWalletId.<ResponseEntity<Map<String, Object>>>map(aLong -> ResponseEntity.ok(Map.of("currentWalletId", aLong))).orElseGet(() -> ResponseEntity.ok(Map.of("message", "There is no current wallet yet")));
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
