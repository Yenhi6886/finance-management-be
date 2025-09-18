package com.example.backend.dto.response;

import com.example.backend.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private Instant date;
    private Long categoryId;
    private String category;
    private Long walletId;
    private String walletName;
    private String fromWalletName;
    private String toWalletName;
    private boolean budgetExceeded;
}