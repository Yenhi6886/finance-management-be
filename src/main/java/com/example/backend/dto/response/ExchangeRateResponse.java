package com.example.backend.dto.response;

import com.example.backend.enums.Currency;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExchangeRateResponse {

    private Long id;
    private Currency currency;
    private BigDecimal rateToVND;
    private LocalDateTime lastUpdated;
    private String updatedBy;
}
