package com.example.backend.dto.response;

import com.example.backend.enums.Currency;
import com.example.backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal budget;
    private Currency currency;
}
