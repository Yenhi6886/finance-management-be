package com.example.backend.controller;

import com.example.backend.annotation.RequireWalletPermission;
import com.example.backend.dto.request.AddMoneyRequest;
import com.example.backend.dto.request.AssignPermissionRequest;
import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.request.WalletTransferRequest;
import com.example.backend.dto.response.*;
import com.example.backend.enums.PermissionType;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/{walletId}/add-money")
    @RequireWalletPermission(value = PermissionType.ADD_TRANSACTION, walletId = "#walletId")
    public ResponseEntity<ApiResponse<TransactionResponse>> addMoney(
            @PathVariable Long walletId,
            @Valid @RequestBody AddMoneyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TransactionResponse transactionResponse = walletService.addMoney(walletId, request, currentUser.getId());
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>(true, "Nạp tiền thành công", transactionResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{walletId}")
    @RequireWalletPermission(value = PermissionType.EDIT_WALLET, walletId = "#walletId")
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
    @RequireWalletPermission(value = PermissionType.VIEW_WALLET, walletId = "#walletId")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse wallet = walletService.getWalletById(walletId, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Lấy thông tin ví thành công", wallet);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{walletId}/details")
    @RequireWalletPermission(value = PermissionType.VIEW_WALLET, walletId = "#walletId")
    public ResponseEntity<ApiResponse<WalletDetailResponse>> getWalletDetails(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletDetailResponse walletDetails = walletService.getWalletDetails(walletId);
        ApiResponse<WalletDetailResponse> apiResponse = new ApiResponse<>(true, "Lấy chi tiết ví thành công", walletDetails);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{walletId}/transactions")
    @RequireWalletPermission(value = PermissionType.VIEW_TRANSACTIONS, walletId = "#walletId")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getWalletTransactions(
            @PathVariable Long walletId,
            Pageable pageable) {
        Page<TransactionResponse> transactions = walletService.getTransactionsByWalletId(walletId, pageable);
        ApiResponse<Page<TransactionResponse>> apiResponse = new ApiResponse<>(true, "Lấy lịch sử giao dịch thành công", transactions);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{walletId}/balance-history")
    @RequireWalletPermission(value = PermissionType.VIEW_BALANCE, walletId = "#walletId")
    public ResponseEntity<ApiResponse<List<BalanceHistoryResponse>>> getBalanceHistory(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "30d") String period) {
        List<BalanceHistoryResponse> history = walletService.getBalanceHistory(walletId, period);
        ApiResponse<List<BalanceHistoryResponse>> apiResponse = new ApiResponse<>(true, "Lấy lịch sử số dư thành công", history);
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

    @PostMapping("/transfer")
    @RequireWalletPermission(value = PermissionType.ADD_TRANSACTION, walletId = "#request.fromWalletId")
    public ResponseEntity<ApiResponse<WalletTransferResponse>> transferMoney(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody WalletTransferRequest request) {
        WalletTransferResponse response = walletTransferService.transferMoney(currentUser.getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Chuyện tiền thành công", response));
    }

    @GetMapping("/{walletId}/validate-amount")
    @RequireWalletPermission(value = PermissionType.VIEW_BALANCE, walletId = "#walletId")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateTransferAmount(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount) {
        boolean isValid = walletTransferService.validateTransferAmount(walletId, currentUser.getId(), amount);
        String message = isValid ? "Số dư đủ để thực hiện giao dịch" : "Số dư không đủ để thực hiện giao dịch";
        return ResponseEntity.ok(new ApiResponse<>(true, message, Map.of("valid", isValid)));
    }

    @DeleteMapping("/{id}/share/{userId}")
    @RequireWalletPermission(value = PermissionType.MANAGE_PERMISSIONS, requireOwnership = true, walletId = "#id")
    public ResponseEntity<ApiResponse<Void>> removeSharedUser(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        walletShareService.removeSharedUser(id, userId, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Xóa người dùng khỏi ví chia sẻ thành công", null);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/share/{userId}/permissions")
    @RequireWalletPermission(value = PermissionType.MANAGE_PERMISSIONS, requireOwnership = true, walletId = "#id")
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
    @RequireWalletPermission(value = PermissionType.DELETE_WALLET, requireOwnership = true, walletId = "#walletId")
    public ResponseEntity<ApiResponse<Void>> deleteWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        walletService.deleteWallet(walletId, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Xóa ví thành công", null);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{walletId}/archive")
    @RequireWalletPermission(value = PermissionType.EDIT_WALLET, walletId = "#walletId")
    public ResponseEntity<ApiResponse<WalletResponse>> archiveWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse walletResponse = walletService.archiveWallet(walletId, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Lưu trữ ví thành công", walletResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{walletId}/unarchive")
    @RequireWalletPermission(value = PermissionType.EDIT_WALLET, walletId = "#walletId")
    public ResponseEntity<ApiResponse<WalletResponse>> unarchiveWallet(
            @PathVariable Long walletId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        WalletResponse walletResponse = walletService.unarchiveWallet(walletId, currentUser.getId());
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>(true, "Khôi phục ví thành công", walletResponse);
        return ResponseEntity.ok(apiResponse);
    }
}