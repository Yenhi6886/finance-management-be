package com.example.backend.dto.response;

import com.example.backend.enums.BudgetPeriod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String color;

    private BigDecimal budgetAmount;
    private BudgetPeriod budgetPeriod;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;

    private BigDecimal incomeTargetAmount;
    private BudgetPeriod incomeTargetPeriod;
    private BigDecimal earnedAmount;
}