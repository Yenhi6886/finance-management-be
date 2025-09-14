package com.example.backend.controller;

import com.example.backend.dto.request.DepositRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest depositRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        TransactionResponse transactionResponse = transactionService.createDeposit(depositRequest, currentUser);

        ApiResponse<TransactionResponse> response = new ApiResponse<>(
                true,
                "Nạp tiền vào ví thành công",
                transactionResponse
        );

        return ResponseEntity.ok(response);
    }
}
