package com.example.backend.dto.response;

import com.example.backend.enums.Currency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponse {
    private Long id;
    private String name;
    private String icon;
    private Currency currency;
    private BigDecimal initialBalance;
    private BigDecimal balance; // For simplicity, balance is same as initialBalance on creation
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}