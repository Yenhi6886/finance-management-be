package com.example.backend.service;

import com.example.backend.dto.request.DepositRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    // Sẽ được implement ở Part 3
    public TransactionResponse createDeposit(DepositRequest request, CustomUserDetails currentUser) {
        // TODO: Implement business logic here
        return null;
    }
}
