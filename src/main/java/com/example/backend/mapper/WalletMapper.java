package com.example.backend.mapper;

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
        // Tạm thời gán quyền là owner, sau này có thể thêm logic phức tạp hơn
        response    .setPermissions("owner");
        return response;
    }
}