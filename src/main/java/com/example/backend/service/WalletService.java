package com.example.backend.service;

import com.example.backend.dto.request.AddMoneyRequest;
import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.response.BalanceHistoryResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.*;
import com.example.backend.enums.PermissionType;
import com.example.backend.enums.TransactionType;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.WalletMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
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

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        String finalDescription = String.format("Nạp tiền qua %s%s",
                request.getMethod(),
                (request.getDescription() != null && !request.getDescription().isBlank()) ? ": " + request.getDescription() : "");

        Transaction transaction = Transaction.builder()
                .user(user)
                .wallet(wallet)
                .type(TransactionType.INCOME)
                .amount(request.getAmount())
                .description(finalDescription)
                .date(LocalDateTime.now())
                .balanceAfterTransaction(newBalance)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .type(savedTransaction.getType())
                .description(savedTransaction.getDescription())
                .date(savedTransaction.getDate())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
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
                    BigDecimal totalDeposited = transactionRepository.calculateTotalByWalletIdAndType(wallet.getId(), TransactionType.INCOME);
                    if (totalDeposited != null) {
                        response.setTotalDeposited(totalDeposited);
                    }
                    return response;
                })
                .collect(Collectors.toList());
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

    public Page<TransactionResponse> getTransactionsByWalletId(Long walletId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByWalletId(walletId, pageable);
        return transactions.map(transaction -> TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .category(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .walletId(transaction.getWallet().getId())
                .walletName(transaction.getWallet().getName())
                .build());
    }

    public List<BalanceHistoryResponse> getBalanceHistory(Long walletId, String period) {
        LocalDateTime startDate;
        switch (period) {
            case "7d":
                startDate = LocalDateTime.now().minusDays(7);
                break;
            case "1m":
                startDate = LocalDateTime.now().minusMonths(1);
                break;
            case "3m":
                startDate = LocalDateTime.now().minusMonths(3);
                break;
            default:
                startDate = LocalDateTime.now().minusDays(30);
                break;
        }

        List<Object[]> results = transactionRepository.findClosingBalanceByDate(walletId, startDate);
        return results.stream()
                .map(result -> new BalanceHistoryResponse(
                        ((Timestamp) result[0]).toLocalDateTime().toLocalDate(),
                        (BigDecimal) result[1]
                ))
                .collect(Collectors.toList());
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