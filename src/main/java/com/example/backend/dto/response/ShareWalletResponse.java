package com.example.backend.dto.response;

import com.example.backend.entity.WalletShare;
import com.example.backend.enums.InvitationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShareWalletResponse {

    private Long id;
    private Long walletId;
    private String walletName;
    private String ownerName;
    private Long sharedWithUserId;
    private String sharedWithEmail;
    private String sharedWithName;
    private WalletShare.PermissionLevel permissionLevel;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private String message;
    private String invitationToken;
    private LocalDateTime expiresAt;
    private WalletResponse wallet;
}