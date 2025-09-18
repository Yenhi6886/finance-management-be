package com.example.backend.dto.response;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public class BudgetStatRespond {
    private BigDecimal totalBudget;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal remainingAmount;
    private Page<TransactionResponse> transactions;
}

