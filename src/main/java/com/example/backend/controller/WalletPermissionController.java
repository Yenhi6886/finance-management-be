package com.example.backend.controller;

import com.example.backend.dto.request.AssignPermissionRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PermissionResponse;
import com.example.backend.dto.response.UserWalletPermissionsResponse;
import com.example.backend.enums.PermissionType;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.WalletPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet-permissions")
@RequiredArgsConstructor
public class WalletPermissionController {

    private final WalletPermissionService walletPermissionService;

    @PostMapping("/{walletId}/users/{userId}")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> assignPermissions(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @Valid @RequestBody AssignPermissionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<PermissionResponse> permissions = walletPermissionService.assignPermissions(
                walletId, userId, request, currentUser.getId());
        
        ApiResponse<List<PermissionResponse>> apiResponse = new ApiResponse<>(
                true, 
                "Gán quyền thành công", 
                permissions
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}/users/{userId}")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getUserPermissions(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<PermissionResponse> permissions = walletPermissionService.getUserPermissions(walletId, userId);
        
        ApiResponse<List<PermissionResponse>> apiResponse = new ApiResponse<>(
                true, 
                "Lấy danh sách quyền thành công", 
                permissions
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/my-permissions")
    public ResponseEntity<ApiResponse<List<UserWalletPermissionsResponse>>> getMyWalletPermissions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<UserWalletPermissionsResponse> permissions = walletPermissionService.getUserWalletPermissions(currentUser.getId());
        
        ApiResponse<List<UserWalletPermissionsResponse>> apiResponse = new ApiResponse<>(
                true, 
                "Lấy danh sách quyền ví của tôi thành công", 
                permissions
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{walletId}/users/{userId}/permissions/{permissionType}")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @PathVariable PermissionType permissionType,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        walletPermissionService.revokePermission(walletId, userId, permissionType, currentUser.getId());
        
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true, 
                "Thu hồi quyền thành công", 
                null
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{walletId}/users/{userId}/has-permission/{permissionType}")
    public ResponseEntity<ApiResponse<Boolean>> hasPermission(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @PathVariable PermissionType permissionType) {

        boolean hasPermission = walletPermissionService.hasPermission(walletId, userId, permissionType);
        
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(
                true, 
                "Kiểm tra quyền thành công", 
                hasPermission
        );
        return ResponseEntity.ok(apiResponse);
    }
}
