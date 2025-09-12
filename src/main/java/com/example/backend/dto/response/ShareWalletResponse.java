package com.example.backend.dto.response;

import com.example.backend.entity.WalletShare;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareWalletResponse {

    private Long id;
    private Long walletId;
    private String walletName;
    private String ownerName;
    private String sharedWithEmail;
    private String sharedWithName;
    private WalletShare.PermissionLevel permissionLevel;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String message;
}
