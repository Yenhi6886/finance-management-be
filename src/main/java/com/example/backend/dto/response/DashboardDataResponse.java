package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardDataResponse {
    private WalletSummaryResponse summary;
    private List<TransactionResponse> recentTransactions;
    private List<CategorySpendingResponse> spendingByCategory;
    private List<IncomeExpenseTrendResponse> incomeExpenseTrend;
    private List<ChartDataPointResponse> weeklySpending;
    private List<CategorySpendingResponse> topSpendingCategories;
}