package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletSummaryResponse {
    private BigDecimal totalBalanceVND;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private int totalWallets;
    private double monthlyGrowth;
    private long totalTransactions;
}