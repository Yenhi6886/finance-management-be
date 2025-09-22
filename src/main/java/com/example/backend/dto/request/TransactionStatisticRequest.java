package com.example.backend.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionStatisticRequest {
    private Long walletId;
    private Long categoryId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
