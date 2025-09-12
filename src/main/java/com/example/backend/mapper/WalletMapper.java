package com.example.backend.mapper;

import com.example.backend.dto.WalletDto;
import com.example.backend.dto.request.CreateWalletRequest;
import com.example.backend.dto.response.WalletResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public Wallet toWallet(CreateWalletRequest request, User user) {
        Wallet wallet = new Wallet();
        wallet.setName(request.getName());
        wallet.setIcon(request.getIcon());
        wallet.setCurrency(request.getCurrency());
        wallet.setInitialBalance(request.getInitialBalance());
        wallet.setDescription(request.getDescription());
        wallet.setUser(user);
        return wallet;
    }

    public WalletResponse toWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setName(wallet.getName());
        response.setIcon(wallet.getIcon());
        response.setCurrency(wallet.getCurrency());
        response.setInitialBalance(wallet.getInitialBalance());
        response.setBalance(wallet.getInitialBalance()); // Logic to calculate current balance can be added later
        response.setDescription(wallet.getDescription());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }
    public WalletDto toWalletDto(Wallet wallet) {
        WalletDto dto = new WalletDto();
        dto.setId(wallet.getId());
        dto.setName(wallet.getName());
        dto.setIcon(wallet.getIcon());
        dto.setCurrency(wallet.getCurrency());
        dto.setInitialBalance(wallet.getInitialBalance());
        dto.setBalance(wallet.getInitialBalance()); // Logic to calculate current balance can be added later
        dto.setDescription(wallet.getDescription());
        dto.setCreatedAt(wallet.getCreatedAt());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }
}