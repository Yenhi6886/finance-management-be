package com.example.backend.controller;

import com.example.backend.dto.request.TransactionRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "50") int limit) {

        List<TransactionResponse> transactions = transactionService.getTransactions(currentUser.getId(), type, categoryId, date, limit);
        ApiResponse<List<TransactionResponse>> response = new ApiResponse<>(true, "Lấy danh sách giao dịch thành công", transactions);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TransactionResponse transaction = transactionService.updateTransaction(id, request, currentUser.getId());
        ApiResponse<TransactionResponse> response = new ApiResponse<>(true, "Cập nhật giao dịch thành công", transaction);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        transactionService.deleteTransaction(id, currentUser.getId());
        ApiResponse<Void> response = new ApiResponse<>(true, "Xóa giao dịch thành công", null);
        return ResponseEntity.ok(response);
    }
}