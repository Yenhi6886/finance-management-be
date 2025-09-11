package com.example.backend.mapper;

import com.example.backend.dto.WalletDto;
import com.example.backend.entity.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletMapper {

    public WalletDto toDto(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        return WalletDto.builder()
                .id(wallet.getId())
                .name(wallet.getName())
                .icon(wallet.getIcon())
                .balance(wallet.getBalance())
                .currencyCode(wallet.getCurrencyCode())
                .description(wallet.getDescription())
                .archived(wallet.isArchived())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    public List<WalletDto> toDtoList(List<Wallet> wallets) {
        if (wallets == null) {
            return null;
        }

        return wallets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
