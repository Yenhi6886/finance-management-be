package com.example.backend.dto.request;

import com.example.backend.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportRequest {
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private List<Long> walletIds;
    
    private List<TransactionType> transactionTypes;
    
    private List<Long> categoryIds;
    
    private String reportType; // "EXCEL" hoặc "PDF"
    
    private String reportFormat; // "DETAILED", "SUMMARY", "CATEGORY"
}
