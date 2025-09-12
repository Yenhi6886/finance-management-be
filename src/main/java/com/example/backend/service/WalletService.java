package com.example.backend.service;

import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.entity.WalletShare;
import com.example.backend.mapper.WalletMapper;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import com.example.backend.repository.WalletShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private WalletShareRepository walletShareRepository;

    public WalletResponse createWallet(CreateWalletRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Wallet wallet = walletMapper.toWallet(request, user);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    public List<WalletResponse> getWalletsByUserId(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getSharedWalletsByUserId(Long userId) {
        List<WalletShare> walletShares = walletShareRepository.findSharedWalletsByUserId(userId);
        return walletShares.stream()
                .map(ws -> {
                    WalletResponse response = walletMapper.toWalletResponse(ws.getWallet());
                    response.setSharedBy(ws.getOwner().getFirstName() + " " + ws.getOwner().getLastName());
                    response.setPermissionLevel(ws.getPermissionLevel().name());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getAllWalletsByUserId(Long userId) {
        // Lấy ví của user
        List<Wallet> userWallets = walletRepository.findByUserId(userId);
        List<WalletResponse> userWalletResponses = userWallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());

        // Lấy ví được share với user
        List<WalletShare> sharedWallets = walletShareRepository.findSharedWalletsByUserId(userId);
        List<WalletResponse> sharedWalletResponses = sharedWallets.stream()
                .map(ws -> {
                    WalletResponse response = walletMapper.toWalletResponse(ws.getWallet());
                    // Thêm thông tin về quyền truy cập
                    response.setSharedBy(ws.getOwner().getFirstName() + " " + ws.getOwner().getLastName());
                    response.setPermissionLevel(ws.getPermissionLevel().name());
                    return response;
                })
                .collect(Collectors.toList());

        // Kết hợp cả hai danh sách
        userWalletResponses.addAll(sharedWalletResponses);
        return userWalletResponses;
    }

    /**
     * Kiểm tra user có phải là chủ sở hữu ví không
     */
    public boolean isWalletOwner(Long walletId, Long userId) {
        return walletRepository.findById(walletId)
                .map(wallet -> wallet.getUser().getId().equals(userId))
                .orElse(false);
    }

}