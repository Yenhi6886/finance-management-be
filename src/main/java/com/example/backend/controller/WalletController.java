package com.example.backend.controller;

import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        WalletResponse walletResponse = walletService.createWallet(request, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Tạo ví thành công", walletResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<WalletResponse> wallets = walletService.getWalletsByUserId(currentUser.getId());
        ApiResponse<List<WalletResponse>> apiResponse = new ApiResponse<>(true, "Lấy danh sách ví thành công", wallets);
        return ResponseEntity.ok(apiResponse);
    }
}