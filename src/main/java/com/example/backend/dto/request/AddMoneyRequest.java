package com.example.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyRequest {

    @NotNull(message = "{validation.notnull.amount}")
    @DecimalMin(value = "1000.0", message = "{validation.min.amount}")
    @DecimalMax(value = "1000000000.0", message = "{validation.max.amount}")
    private BigDecimal amount;

    @NotEmpty(message = "{validation.notempty.method}")
    private String method;

    private String description;
}