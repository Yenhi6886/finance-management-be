package com.example.backend.service;

import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.request.UpdateWalletRequest;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.mapper.WalletMapper;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    public WalletResponse getWalletById(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ví với id: " + walletId));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem ví này");
        }

        return walletMapper.toWalletResponse(wallet);
    }

    public WalletResponse updateWallet(Long walletId, UpdateWalletRequest request, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ví với id: " + walletId));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa ví này");
        }

        wallet.setName(request.getName());
        wallet.setIcon(request.getIcon());
        wallet.setCurrency(request.getCurrency());
        wallet.setDescription(request.getDescription());

        Wallet updatedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(updatedWallet);
    }

    public void deleteWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ví với id: " + walletId));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa ví này");
        }

        walletRepository.delete(wallet);
    }
}