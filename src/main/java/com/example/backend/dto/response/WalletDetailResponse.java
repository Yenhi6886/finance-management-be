package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WalletDetailResponse {
    private WalletResponse wallet;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal netChange;
    private List<BalanceHistoryResponse> balanceHistory;
    private List<CategorySpendingResponse> expenseByCategory;
}