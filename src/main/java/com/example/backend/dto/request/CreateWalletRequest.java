package com.example.backend.dto.request;

import com.example.backend.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletRequest {

    @NotBlank(message = "Tên ví không được để trống")
    @Size(max = 100, message = "Tên ví không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Icon không được để trống")
    @Size(max = 10, message = "Icon không được vượt quá 10 ký tự")
    private String icon;

    @NotNull(message = "Loại tiền tệ không được để trống")
    private Currency currency;

    @NotNull(message = "Số tiền ban đầu không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền ban đầu phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "1000000000000.0", inclusive = true, message = "Số tiền ban đầu không được vượt quá 1 nghìn tỷ")
    private BigDecimal balance;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}