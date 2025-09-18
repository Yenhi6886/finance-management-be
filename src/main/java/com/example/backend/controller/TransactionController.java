package com.example.backend.controller;

import com.example.backend.dto.request.TransactionRequest;
import com.example.backend.dto.request.TransactionStatisticRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.entity.Transaction;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @RequestParam(defaultValue = "50") int limit) {

        List<TransactionResponse> transactions = transactionService.getTransactions(currentUser.getId(), type, limit);
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

    @GetMapping("/statistics/today" )
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTodayTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<TransactionResponse> transactions = transactionService.getTransactionsToday(currentUser.getId(), pageable);
        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(true, "Lấy danh sách giao dịch hôm nay thành công", transactions);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/statistics" )
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionStatistics(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody TransactionStatisticRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<TransactionResponse> transactions = transactionService.getTransactionsByTime(currentUser.getId(), request.getStartDate(), request.getEndDate(), pageable);
        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(true, "Lấy danh sách giao dịch theo thời gian thành công", transactions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/wallet/today")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTodayWalletTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody TransactionStatisticRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<TransactionResponse> transactions = transactionService.getTransactionsTodayByWalletId(currentUser.getId(), request.getWalletId(), pageable);
        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(true, "Lấy danh sách giao dịch hôm nay theo ví thành công", transactions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("Statistics/wallet")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getWalletTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody TransactionStatisticRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<TransactionResponse> transactions = transactionService.getTransactionsByWalletIdAndTime(currentUser.getId(), request.getWalletId(), request.getStartDate(), request.getEndDate(), pageable);
        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(true, "Lấy danh sách giao dịch theo thời gian và ví thành công", transactions);
        return ResponseEntity.ok(response);
    }
}