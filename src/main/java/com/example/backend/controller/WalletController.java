package com.example.backend.controller;

import com.example.backend.dto.WalletDto;
import com.example.backend.dto.response.WalletTransferResponse;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.entity.Wallet;
import com.example.backend.exception.InsufficientBalanceException;
import com.example.backend.exception.WalletNotFoundException;
import com.example.backend.mapper.WalletMapper;
import com.example.backend.service.WalletSelectionService;
import com.example.backend.service.WalletTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletSelectionService walletSelectionService;
    private final WalletMapper walletMapper;
    private final WalletTransferService walletTransferService;

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
    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody UpdateProfileRequest.WalletTransferRequest request) {
        try {
            WalletTransferResponse response = walletTransferService.transferMoney(userId, request);
            return ResponseEntity.ok(response);
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Có lỗi xảy ra khi chuyển tiền"));
        }
    }

    @GetMapping("/{walletId}/validate-amount")
    public ResponseEntity<Map<String, Object>> validateTransferAmount(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount) {
        try {
            boolean isValid = walletTransferService.validateTransferAmount(walletId, userId, amount);
            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "Số dư đủ để thực hiện giao dịch" : "Số dư không đủ để thực hiện giao dịch"
            ));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("valid", false, "message", "Có lỗi xảy ra khi kiểm tra số dư"));
        }
    }
}

