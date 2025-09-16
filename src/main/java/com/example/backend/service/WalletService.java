package com.example.backend.service;

import com.example.backend.dto.request.AddMoneyRequest;
import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.*;
import com.example.backend.enums.PermissionType;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.WalletMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final WalletShareRepository walletShareRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final WalletPermissionRepository walletPermissionRepository;

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Wallet wallet = walletMapper.toWallet(request, user);
        Wallet savedWallet = walletRepository.save(wallet);

        grantFullPermissionsToOwner(savedWallet, user);

        return walletMapper.toWalletResponse(savedWallet);
    }

    @Transactional
    public TransactionResponse addMoney(Long walletId, AddMoneyRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        checkIfWalletIsArchived(wallet);

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        String finalDescription = String.format("Nạp tiền qua %s%s",
                request.getMethod(),
                (request.getDescription() != null && !request.getDescription().isBlank()) ? ": " + request.getDescription() : "");

        // Get or create default income category for money deposits
        TransactionCategory incomeCategory = getDefaultIncomeCategory();

        Transaction transaction = Transaction.builder()
                .user(user)
                .wallet(wallet)
                .category(incomeCategory)
                .amount(request.getAmount())
                .description(finalDescription)
                .date(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .date(savedTransaction.getDate())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .categoryName(incomeCategory.getName()) // Only use category name
                .build();
    }

    private void grantFullPermissionsToOwner(Wallet wallet, User owner) {
        WalletShare ownerShare = WalletShare.builder()
                .wallet(wallet)
                .owner(owner)
                .sharedWithUser(owner)
                .isActive(true)
                .permissionLevel(WalletShare.PermissionLevel.OWNER)
                .build();
        WalletShare savedShare = walletShareRepository.save(ownerShare);

        List<WalletPermission> permissions = new ArrayList<>();
        for (PermissionType type : PermissionType.values()) {
            permissions.add(WalletPermission.builder()
                    .walletShare(savedShare)
                    .permissionType(type)
                    .isGranted(true)
                    .build());
        }
        walletPermissionRepository.saveAll(permissions);
    }

    public List<WalletResponse> getWalletsForDashboard(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdAndIsArchived(userId, false);
        return wallets.stream()
                .map(wallet -> {
                    WalletResponse response = walletMapper.toWalletResponse(wallet);
                    // Calculate total deposited by finding all income categories (by name pattern) and summing their transactions
                    List<TransactionCategory> incomeCategories = transactionCategoryRepository.findByNameContainingIgnoreCase("thu");
                    List<Long> incomeCategoryIds = incomeCategories.stream()
                            .map(TransactionCategory::getId)
                            .collect(Collectors.toList());

                    if (!incomeCategoryIds.isEmpty()) {
                        BigDecimal totalDeposited = transactionRepository.calculateTotalByWalletIdAndCategories(wallet.getId(), incomeCategoryIds);
                        if (totalDeposited != null) {
                            response.setTotalDeposited(totalDeposited);
                        }
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    private TransactionCategory getDefaultIncomeCategory() {
        // Try to find existing income category by name pattern
        List<TransactionCategory> incomeCategories = transactionCategoryRepository.findByNameContainingIgnoreCase("thu");

        if (!incomeCategories.isEmpty()) {
            // Return the first income category found
            return incomeCategories.get(0);
        }

        // Create default income category if none exists
        TransactionCategory defaultIncomeCategory = TransactionCategory.builder()
                .name("Thu nhập - Nạp tiền")
                .description("Danh mục mặc định cho việc nạp tiền vào ví")
                .budget(BigDecimal.ZERO)
                .build();

        return transactionCategoryRepository.save(defaultIncomeCategory);
    }

    @Transactional
    public WalletResponse updateWallet(Long walletId, UpdateWalletRequest request, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        checkIfWalletIsArchived(wallet);

        wallet.setName(request.getName());
        wallet.setIcon(request.getIcon());
        wallet.setBalance(request.getBalance());
        wallet.setCurrency(request.getCurrency());
        wallet.setDescription(request.getDescription());

        Wallet updatedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(updatedWallet);
    }

    public WalletResponse getWalletById(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        if (!isWalletOwner(walletId, userId) && !walletShareRepository.existsByWalletIdAndSharedWithUserIdAndIsActiveTrue(walletId, userId)) {
            throw new BadRequestException("Bạn không có quyền truy cập ví này");
        }

        return walletMapper.toWalletResponse(wallet);
    }

    public List<WalletResponse> getWalletsByUserId(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdAndIsArchived(userId, false);
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getSharedWalletsByUserId(Long userId) {
        List<WalletShare> walletShares = walletShareRepository.findSharedWalletsByUserId(userId);
        return walletShares.stream()
                .filter(ws -> !ws.getWallet().isArchived() && !ws.getOwner().getId().equals(userId))
                .map(ws -> {
                    WalletResponse response = walletMapper.toWalletResponse(ws.getWallet());
                    response.setSharedBy(ws.getOwner().getFirstName() + " " + ws.getOwner().getLastName());
                    response.setPermissionLevel(ws.getPermissionLevel().name());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getAllWalletsByUserId(Long userId) {
        List<Wallet> userWallets = walletRepository.findByUserIdAndIsArchived(userId, false);
        List<WalletResponse> userWalletResponses = userWallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());

        List<WalletShare> sharedWallets = walletShareRepository.findSharedWalletsByUserId(userId);
        List<WalletResponse> sharedWalletResponses = sharedWallets.stream()
                .filter(ws -> !ws.getWallet().isArchived() && !ws.getOwner().getId().equals(userId))
                .map(ws -> {
                    WalletResponse response = walletMapper.toWalletResponse(ws.getWallet());
                    response.setSharedBy(ws.getOwner().getFirstName() + " " + ws.getOwner().getLastName());
                    response.setPermissionLevel(ws.getPermissionLevel().name());
                    return response;
                })
                .collect(Collectors.toList());

        userWalletResponses.addAll(sharedWalletResponses);
        return userWalletResponses;
    }

    public List<WalletResponse> getArchivedWalletsByUserId(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdAndIsArchived(userId, true);
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    public boolean isWalletOwner(Long walletId, Long userId) {
        return walletRepository.existsByIdAndUserId(walletId, userId);
    }

    @Transactional
    public void deleteWallet(Long walletId, Long userId) {
        Wallet walletToDelete = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        checkIfWalletIsArchived(walletToDelete);

        transactionRepository.deleteByWalletId(walletId);

        List<UserSettings> settingsToUpdate = userSettingsRepository.findByCurrentWalletId(walletId);
        settingsToUpdate.forEach(setting -> setting.setCurrentWallet(null));
        userSettingsRepository.saveAll(settingsToUpdate);

        walletShareRepository.deleteByWalletId(walletId);
        walletRepository.delete(walletToDelete);
    }

    @Transactional
    public WalletResponse archiveWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));
        wallet.setArchived(true);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    @Transactional
    public WalletResponse unarchiveWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));
        wallet.setArchived(false);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    private void checkIfWalletIsArchived(Wallet wallet) {
        if (wallet.isArchived()) {
            throw new BadRequestException("Ví đã được lưu trữ và không thể thực hiện hành động này.");
        }
    }
}
