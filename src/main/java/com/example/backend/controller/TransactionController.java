package com.example.backend.controller;

import com.example.backend.dto.request.ExpenseCreateRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<TransactionResponse>> createExpense(
            @Valid @RequestBody ExpenseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        TransactionResponse expense = transactionService.createExpense(request, currentUser.getId());
        ApiResponse<TransactionResponse> response = new ApiResponse<>(true, "Ghi lại khoản chi thành công", expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<TransactionResponse> transactions = transactionService.getTransactions(currentUser.getId(), type, limit);
        ApiResponse<List<TransactionResponse>> response = new ApiResponse<>(true, "Lấy danh sách giao dịch thành công", transactions);
        return ResponseEntity.ok(response);
    }
}