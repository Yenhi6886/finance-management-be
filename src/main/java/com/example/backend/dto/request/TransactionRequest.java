package com.example.backend.dto.request;

import com.example.backend.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionRequest {
    @NotNull(message = "{validation.notnull.amount}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.min.amount.positive}")
    private BigDecimal amount;

    @NotNull(message = "{validation.notnull.transaction.type}")
    private TransactionType type;

    @NotNull(message = "{validation.notnull.wallet}")
    private Long walletId;

    private Long categoryId;

    private String description;

    @NotNull(message = "{validation.notnull.date}")
    private Instant date;
}