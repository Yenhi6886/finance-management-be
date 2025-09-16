package com.example.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCreateRequest {

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Danh mục chi không được để trống")
    private Long categoryId;

    @NotNull(message = "Ví chi tiền không được để trống")
    private Long walletId;

    private String description; // Ghi chú

    private LocalDateTime transactionDate; // Thời gian (mặc định lấy thời gian hiện tại nếu null)
}
