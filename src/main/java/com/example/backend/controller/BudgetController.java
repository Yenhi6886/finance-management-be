package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.BudgetStatRespond;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BudgetStatRespond>> getBudgetStatistics(
           @AuthenticationPrincipal CustomUserDetails currentUser,
           @RequestParam int year,
           @RequestParam int month,
           // Thêm RequestParam cho page và size với giá trị mặc định
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size) {
        BudgetStatRespond budgetStat = budgetService.getBudgetStat(currentUser.getId(), year, month, page, size);
        ApiResponse<BudgetStatRespond> response = new ApiResponse<>(true, "Lấy thống kê ngân sách thành công", budgetStat);
        return ResponseEntity.ok(response);
    }
}
