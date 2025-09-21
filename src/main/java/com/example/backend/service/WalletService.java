package com.example.backend.service;

import com.example.backend.dto.request.AddMoneyRequest;
import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.response.*;
import com.example.backend.entity.*;
import com.example.backend.enums.InvitationStatus;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
                .date(Instant.now())
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
                .status(InvitationStatus.ACCEPTED)
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
        wallet.setDescription(request.getDescription());

        Wallet updatedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(updatedWallet);
    }

    public WalletResponse getWalletById(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        if (!isWalletOwner(walletId, userId) && !walletShareRepository.existsByWalletIdAndSharedWithUserIdAndStatus(walletId, userId, InvitationStatus.ACCEPTED)) {
            throw new BadRequestException("Bạn không có quyền truy cập ví này");
        }

        return walletMapper.toWalletResponse(wallet);
    }

    public WalletDetailResponse getWalletDetails(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        YearMonth currentMonth = YearMonth.now();
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        BigDecimal monthlyIncome = transactionRepository.sumAmountByWalletIdAndTypeAndDateBetween(walletId, TransactionType.INCOME, startOfMonth, endOfMonth);
        BigDecimal monthlyExpense = transactionRepository.sumAmountByWalletIdAndTypeAndDateBetween(walletId, TransactionType.EXPENSE, startOfMonth, endOfMonth);
        BigDecimal netChange = monthlyIncome.subtract(monthlyExpense);

        List<BalanceHistoryResponse> balanceHistory = getBalanceHistory(walletId, "30d");
        List<CategorySpendingResponse> expenseByCategory = transactionRepository.findExpenseByWalletIdAndDateRange(walletId, startOfMonth, endOfMonth);

        return WalletDetailResponse.builder()
                .wallet(walletMapper.toWalletResponse(wallet))
                .monthlyIncome(monthlyIncome)
                .monthlyExpense(monthlyExpense)
                .netChange(netChange)
                .balanceHistory(balanceHistory)
                .expenseByCategory(expenseByCategory)
                .build();
    }

    public List<WalletResponse> getWalletsByUserId(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdAndIsArchived(userId, false);
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getSharedWalletsByUserId(Long userId) {
        List<WalletShare> walletShares = walletShareRepository.findBySharedWithUserIdAndStatus(userId, InvitationStatus.ACCEPTED);
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

        List<WalletShare> sharedWallets = walletShareRepository.findBySharedWithUserIdAndStatus(userId, InvitationStatus.ACCEPTED);
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
        Instant startDate;
        switch (period) {
            case "7d":
                startDate = Instant.now().minus(7, ChronoUnit.DAYS);
                break;
            case "1m":
                startDate = Instant.now().minus(30, ChronoUnit.DAYS);
                break;
            case "3m":
                startDate = Instant.now().minus(90, ChronoUnit.DAYS);
                break;
            default:
                startDate = Instant.now().minus(30, ChronoUnit.DAYS);
                break;
        }

        List<Transaction> transactions = transactionRepository.findByWalletIdAndDateAfterOrderByDateAsc(walletId, startDate);
        if (transactions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocalDate, BigDecimal> closingBalances = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            LocalDate date = t.getDate().atZone(ZoneOffset.UTC).toLocalDate();
            closingBalances.put(date, t.getBalanceAfterTransaction());
        }

        return closingBalances.entrySet().stream()
                .map(entry -> new BalanceHistoryResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(BalanceHistoryResponse::getDate))
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