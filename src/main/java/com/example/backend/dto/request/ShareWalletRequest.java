package com.example.backend.dto.request;

import com.example.backend.entity.WalletShare;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShareWalletRequest {

    @NotNull(message = "ID ví không được để trống")
    private Long walletId;

    @NotNull(message = "Cấp độ quyền không được để trống")
    private WalletShare.PermissionLevel permissionLevel;

    @NotBlank(message = "Phương thức chia sẻ không được để trống")
    private String shareMethod; // email, link, sms

    private List<@Email(message = "Định dạng email không đúng") String> recipients;

    private String message;

    private LocalDateTime expiryDate;

    private String shareToken;
}
