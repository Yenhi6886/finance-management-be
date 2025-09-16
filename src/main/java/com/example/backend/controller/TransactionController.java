package com.example.backend.controller;

import com.example.backend.dto.request.TransactionRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TransactionResponse transaction = transactionService.createTransaction(request, currentUser.getId());
        ApiResponse<TransactionResponse> response = new ApiResponse<>(true, "Tạo giao dịch thành công", transaction);
        return ResponseEntity.ok(response);
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