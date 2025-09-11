package com.example.backend.dto;

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
    private String currencyCode;
    private String description;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
