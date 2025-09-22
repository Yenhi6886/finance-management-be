package com.example.backend.dto.request;

import com.example.backend.enums.BudgetPeriod;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;

    private BigDecimal budgetAmount;

    private BudgetPeriod budgetPeriod;

    private BigDecimal incomeTargetAmount;

    private BudgetPeriod incomeTargetPeriod;
}