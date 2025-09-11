package com.example.backend.dto.response;

import com.example.backend.entity.WalletShare;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareWalletResponse {

    private Long id;
    private Long walletId;
    private String walletName;
    private String walletIcon;
    private WalletShare.PermissionLevel permissionLevel;
    private String shareMethod;
    private List<String> recipients;
    private String message;
    private String shareToken;
    private String shareLink;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
