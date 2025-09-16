package com.example.backend.controller;

import com.example.backend.dto.request.ExpenseCreateRequest;
import com.example.backend.dto.request.ExpenseUpdateRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<TransactionResponse>> createExpense(
            @Valid @RequestBody ExpenseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("=== CREATE EXPENSE API CALLED ===");
        log.info("Request: {}", request);
        log.info("User ID: {}", currentUser != null ? currentUser.getId() : "NULL");

        try {
            TransactionResponse expense = transactionService.createExpense(request, currentUser.getId());
            log.info("Service returned expense: {}", expense);

            ApiResponse<TransactionResponse> response = new ApiResponse<>(true, "Ghi lại khoản chi thành công", expense);
            log.info("Created API response: {}", response);
            
            ResponseEntity<ApiResponse<TransactionResponse>> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(response);
            log.info("Final response entity: {}", responseEntity);
            
            return responseEntity;
        } catch (Exception e) {
            log.error("Error in createExpense", e);
            throw e;
        }
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<TransactionResponse> transactions = transactionService.getAllTransactions(currentUser.getId());
        ApiResponse<List<TransactionResponse>> response = new ApiResponse<>(true, "Lấy tất cả giao dịch thành công", transactions);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody ExpenseUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TransactionResponse updatedTransaction = transactionService.updateExpense(transactionId, request, currentUser.getId());
        ApiResponse<TransactionResponse> response = new ApiResponse<>(true, "Cập nhật giao dịch thành công", updatedTransaction);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        transactionService.deleteTransaction(transactionId, currentUser.getId());
        ApiResponse<Void> response = new ApiResponse<>(true, "Xóa giao dịch thành công");
        return ResponseEntity.ok(response);
    }
}