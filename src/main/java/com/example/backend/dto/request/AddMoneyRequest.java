package com.example.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyRequest {

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000.0", message = "Số tiền tối thiểu là 1,000 VND")
    @DecimalMax(value = "1000000000.0", message = "Số tiền nạp mỗi lần tối đa là 1,000,000,000 VND (1 tỉ)")
    private BigDecimal amount;

    @NotEmpty(message = "Phương thức nạp tiền không được để trống")
    private String method;

    private String description;
}