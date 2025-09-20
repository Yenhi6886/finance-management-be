package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.DashboardDataResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDataResponse>> getDashboardSummary(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(value = "walletId", required = false) Long walletId) {
        DashboardDataResponse data = dashboardService.getDashboardData(currentUser.getId(), walletId);
        ApiResponse<DashboardDataResponse> response = new ApiResponse<>(true, "Lấy dữ liệu dashboard thành công", data);
        return ResponseEntity.ok(response);
    }
}