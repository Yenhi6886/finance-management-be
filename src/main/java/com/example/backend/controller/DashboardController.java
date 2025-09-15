package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final WalletService walletService;

    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getWalletsForDashboard(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<WalletResponse> wallets = walletService.getWalletsForDashboard(currentUser.getId());
        ApiResponse<List<WalletResponse>> response = new ApiResponse<>(true, "Lấy danh sách ví cho dashboard thành công", wallets);
        return ResponseEntity.ok(response);
    }
}