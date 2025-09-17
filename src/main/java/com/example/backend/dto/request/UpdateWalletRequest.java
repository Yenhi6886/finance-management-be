package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWalletRequest {

    @NotBlank(message = "Tên ví là bắt buộc")
    @Size(min = 2, max = 50, message = "Tên ví phải có từ 2 đến 50 ký tự")
    private String name;

    @NotBlank(message = "Icon là bắt buộc")
    private String icon;

    @Size(max = 200, message = "Mô tả không được quá 200 ký tự")
    private String description;
}