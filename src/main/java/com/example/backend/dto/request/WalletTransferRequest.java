package com.example.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransferRequest {

    @NotNull(message = "{validation.notnull.from.wallet}")
    private Long fromWalletId;

    @NotNull(message = "{validation.notnull.to.wallet}")
    private Long toWalletId;

    @NotNull(message = "{validation.notnull.amount}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.min.amount.positive}")
    private BigDecimal amount;

    private String description;

    private Instant date;
}