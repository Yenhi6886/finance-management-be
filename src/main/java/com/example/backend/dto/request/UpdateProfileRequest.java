package com.example.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletTransferRequest {

        @NotNull(message = "Ví nguồn không được để trống")
        private Long fromWalletId;

        @NotNull(message = "Ví đích không được để trống")
        private Long toWalletId;

        @NotNull(message = "Số tiền chuyển không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền chuyển phải lớn hơn 0")
        private BigDecimal amount;

        private String description;
    }
}