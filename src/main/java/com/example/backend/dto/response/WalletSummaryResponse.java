package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletSummaryResponse {
    private Map<String, BigDecimal> totalBalanceByCurrency;
    private BigDecimal totalBalanceVND;
    private int totalWallets;
    private String message;
}