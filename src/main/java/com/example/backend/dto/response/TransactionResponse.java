package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private Long walletId;
    private String walletName;
    private String categoryName; // Only keep category name, remove categoryType

}