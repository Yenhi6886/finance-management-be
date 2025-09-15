package com.example.backend.controller;

import com.example.backend.annotation.RequireWalletPermission;
import com.example.backend.dto.request.AssignPermissionRequest;
import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.ShareWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.request.WalletTransferRequest;
import com.example.backend.dto.response.*;
import com.example.backend.enums.PermissionType;
import com.example.backend.exception.InsufficientBalanceException;
import com.example.backend.exception.WalletNotFoundException;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletSelectionService walletSelectionService;
    private final WalletTransferService walletTransferService;
    private final WalletShareService walletShareService;
    private final WalletPermissionService walletPermissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse walletResponse = walletService.createWallet(request, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Tạo ví mới thành công", walletResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> updateWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody UpdateWalletRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse walletResponse = walletService.updateWallet(walletId, request, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Cập nhật ví thành công", walletResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getAllWallets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<WalletResponse> wallets = walletService.getAllWalletsByUserId(currentUser.getId());
        ApiResponse<List<WalletResponse>> apiResponse = new ApiResponse<>(true, "Lấy danh sách tất cả các ví thành công", wallets);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse wallet = walletService.getWalletById(walletId, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Lấy thông tin ví thành công", wallet);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/my-wallets")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getMyWallets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<WalletResponse> wallets = walletService.getWalletsByUserId(currentUser.getId());
        ApiResponse<List<WalletResponse>> apiResponse = new ApiResponse<>(true, "Lấy danh sách ví của tôi thành công", wallets);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getSharedWallets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<WalletResponse> wallets = walletService.getSharedWalletsByUserId(currentUser.getId());
        ApiResponse<List<WalletResponse>> apiResponse = new ApiResponse<>(true, "Lấy danh sách ví được chia sẻ thành công", wallets);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getArchivedWallets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<WalletResponse> wallets = walletService.getArchivedWalletsByUserId(currentUser.getId());
        ApiResponse<List<WalletResponse>> apiResponse = new ApiResponse<>(true, "Lấy danh sách ví đã lưu trữ thành công", wallets);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCurrentWallet(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<Long> currentWalletIdOpt = walletSelectionService.getCurrentSelectedWalletId(currentUser.getId());
        if (currentWalletIdOpt.isPresent()) {
            ApiResponse<Map<String, Long>> response = new ApiResponse<>(true, "Lấy ví hiện tại thành công", Map.of("currentWalletId", currentWalletIdOpt.get()));
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Map<String, Long>> response = new ApiResponse<>(false, "Chưa có ví nào được chọn", null);
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/current/{walletId}")
    public ResponseEntity<ApiResponse<Void>> setCurrentWallet(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long walletId) {
        walletSelectionService.setCurrentSelectedWallet(currentUser.getId(), walletId);
        ApiResponse<Void> response = new ApiResponse<>(true, "Chọn ví hiện tại thành công", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-balance")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> getTotalBalance(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Map<String, BigDecimal> totalBalanceByCurrency = walletSelectionService.getTotalBalanceByCurrency(currentUser.getId());
        BigDecimal totalBalanceVND = walletSelectionService.getTotalBalanceInVND(currentUser.getId());
        int totalWallets = walletService.getAllWalletsByUserId(currentUser.getId()).size();

        WalletSummaryResponse summaryResponse = WalletSummaryResponse.builder()
                .totalBalanceByCurrency(totalBalanceByCurrency)
                .totalBalanceVND(totalBalanceVND)
                .totalWallets(totalWallets)
                .message("Lấy tổng số dư thành công")
                .build();
        ApiResponse<WalletSummaryResponse> apiResponse = new ApiResponse<>(true, "Lấy tổng số dư thành công", summaryResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<?>> transferMoney(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody WalletTransferRequest request) {
        try {
            WalletTransferResponse response = walletTransferService.transferMoney(currentUser.getId(), request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chuyện tiền thành công", response));
        } catch (InsufficientBalanceException | WalletNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{walletId}/validate-amount")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateTransferAmount(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount) {
        try {
            boolean isValid = walletTransferService.validateTransferAmount(walletId, currentUser.getId(), amount);
            String message = isValid ? "Số dư đủ để thực hiện giao dịch" : "Số dư không đủ để thực hiện giao dịch";
            return ResponseEntity.ok(new ApiResponse<>(true, message, Map.of("valid", isValid)));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), Map.of("valid", false)));
        }
    }

    @DeleteMapping("/{id}/share/{userId}")
    @RequireWalletPermission(value = PermissionType.MANAGE_PERMISSIONS, requireOwnership = true)
    public ResponseEntity<ApiResponse<Void>> removeSharedUser(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        walletShareService.removeSharedUser(id, userId, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Xóa người dùng khỏi ví chia sẻ thành công", null);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/share/{userId}/permissions")
    @RequireWalletPermission(value = PermissionType.MANAGE_PERMISSIONS, requireOwnership = true)
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> updateSharedUserPermissions(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody AssignPermissionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<PermissionResponse> permissions = walletPermissionService.assignPermissions(id, userId, request, currentUser.getId());
        ApiResponse<List<PermissionResponse>> apiResponse = new ApiResponse<>(true, "Cập nhật quyền chia sẻ ví thành công", permissions);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<ApiResponse<Void>> deleteWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        walletService.deleteWallet(walletId, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Xóa ví thành công", null);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{walletId}/archive")
    public ResponseEntity<ApiResponse<WalletResponse>> archiveWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse walletResponse = walletService.archiveWallet(walletId, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Lưu trữ ví thành công", walletResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{walletId}/unarchive")
    public ResponseEntity<ApiResponse<WalletResponse>> unarchiveWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse walletResponse = walletService.unarchiveWallet(walletId, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Khôi phục ví thành công", walletResponse);
        return ResponseEntity.ok(apiResponse);
    }
}