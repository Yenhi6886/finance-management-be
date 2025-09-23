package com.example.backend.dto.request;

import com.example.backend.entity.UserSettings;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSettingsRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.min.exchange.rate}")
    @Digits(integer = 10, fraction = 4, message = "{validation.digits.exchange.rate}")
    private BigDecimal usdToVndRate;

    private UserSettings.CurrencyFormat currencyFormat;

    private UserSettings.DateFormat dateFormat;
}