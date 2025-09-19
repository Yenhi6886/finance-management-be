package com.example.backend.dto.request;

import com.example.backend.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportRequest {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    
    private List<Long> walletIds;
    
    private List<TransactionType> transactionTypes;
    
    private List<Long> categoryIds;
    
    private String reportType; // "EXCEL" hoặc "PDF"
    
    private String reportFormat; // "DETAILED", "SUMMARY", "CATEGORY"
}
