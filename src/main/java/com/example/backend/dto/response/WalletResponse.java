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
    private BigDecimal balance;
    private String description;
    private boolean isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // THÊM TRƯỜNG MỚI
    private BigDecimal totalDeposited;

    private String sharedBy;
    private String permissionLevel;
}