package com.example.backend.dto.request;

import com.example.backend.entity.WalletShare;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareWalletRequest {

    @NotNull(message = "ID ví không được để trống")
    private Long walletId;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotNull(message = "Quyền truy cập không được để trống")
    private WalletShare.PermissionLevel permissionLevel;

    private String message;
    private LocalDateTime expiryDate;
}
