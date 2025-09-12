package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransferResponse {
    private String message;
    private Long transactionId;
    private BigDecimal fromWalletBalance;
    private BigDecimal toWalletBalance;
    private LocalDateTime transferTime;
    private boolean success;
}
