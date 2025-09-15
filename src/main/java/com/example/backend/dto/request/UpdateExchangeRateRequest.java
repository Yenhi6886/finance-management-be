package com.example.backend.dto.request;

import com.example.backend.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateExchangeRateRequest {

    @NotNull(message = "Loại tiền tệ không được để trống")
    private Currency currency;

    @NotNull(message = "Tỷ giá không được để trống")
    @DecimalMin(value = "0.00000001", message = "Tỷ giá phải lớn hơn 0")
    private BigDecimal rateToVND;
}
