package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
@Data
@Builder
public class BudgetStatRespond {
    private BigDecimal totalBudget;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal remainingAmount;
    private Page<TransactionResponse> transactions;
}

