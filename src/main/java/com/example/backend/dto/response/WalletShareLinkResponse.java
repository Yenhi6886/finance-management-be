package com.example.backend.dto.response;

import com.example.backend.entity.WalletShareLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletShareLinkResponse {

    private Long id;
    private Long walletId;
    private String walletName;
    private String ownerName;
    private String shareToken;
    private WalletShareLink.PermissionLevel permissionLevel;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private WalletResponse wallet;
}
