package com.example.backend.dto;

import com.example.backend.enums.Currency;
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
public class WalletDto {
    private Long id;
    private String name;
    private String icon;
    private BigDecimal balance;
    private BigDecimal initialBalance;
    private Currency currency;
    private String description;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
