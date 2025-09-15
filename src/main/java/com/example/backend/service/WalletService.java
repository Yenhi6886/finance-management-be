package com.example.backend.service;

import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.UserSettings;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
import com.example.backend.enums.TransactionType;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.WalletMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    // THÊM MỚI
    private final TransactionRepository transactionRepository;

    public WalletResponse createWallet(CreateWalletRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Wallet wallet = walletMapper.toWallet(request, user);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    // THÊM PHƯƠNG THỨC MỚI
    public List<WalletResponse> getWalletsForDashboard(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserIdAndIsArchived(userId, false);
        return wallets.stream()
                .map(wallet -> {
                    WalletResponse response = walletMapper.toWalletResponse(wallet);
                    BigDecimal totalDeposited = transactionRepository.calculateTotalByWalletIdAndType(wallet.getId(),
                            TransactionType.INCOME);
                    response.setTotalDeposited(totalDeposited);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public WalletResponse updateWallet(Long walletId, UpdateWalletRequest request, Long userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ví hoặc bạn không có quyền chỉnh sửa ví này"));

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

        if (!wallet.getUser().getId().equals(userId)) {
            boolean isShared = walletShareRepository.findByWalletIdAndUserId(walletId, userId).isPresent();
            if (!isShared) {
                throw new BadRequestException("Bạn không có quyền truy cập ví này");
            }
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
                .filter(ws -> !ws.getWallet().isArchived())
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
                .filter(ws -> !ws.getWallet().isArchived())
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
        return walletRepository.findById(walletId)
                .map(wallet -> wallet.getUser().getId().equals(userId))
                .orElse(false);
    }

    @Transactional
    public void deleteWallet(Long walletId, Long userId) {
        Wallet walletToDelete = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy ví hoặc bạn không có quyền xóa ví này"));

        checkIfWalletIsArchived(walletToDelete);

        // Xóa hết số tiền có trong ví
        walletToDelete.setBalance(BigDecimal.ZERO);

        // Xóa hết các giao dịch của ví
        transactionRepository.deleteByWalletId(walletId);

        // Cập nhật user settings nếu có
        List<UserSettings> settingsToUpdate = userSettingsRepository.findByCurrentWalletId(walletId);
        settingsToUpdate.forEach(setting -> setting.setCurrentWallet(null));
        userSettingsRepository.saveAll(settingsToUpdate);

        // Xóa các chia sẻ ví
        walletShareRepository.deleteByWalletId(walletId);

        // Xóa ví
        walletRepository.delete(walletToDelete);
    }

    @Transactional
    public WalletResponse archiveWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ví hoặc bạn không có quyền thực hiện hành động này"));
        wallet.setArchived(true);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    @Transactional
    public WalletResponse unarchiveWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ví hoặc bạn không có quyền thực hiện hành động này"));
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