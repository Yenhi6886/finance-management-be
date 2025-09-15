package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {

    private final Map<String, Long> blacklistedTokenToExpiryEpochMs = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryEpochMs) {
        blacklistedTokenToExpiryEpochMs.put(token, expiryEpochMs);
    }

    public boolean isBlacklisted(String token) {
        Long expiry = blacklistedTokenToExpiryEpochMs.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry < Instant.now().toEpochMilli()) {
            blacklistedTokenToExpiryEpochMs.remove(token);
            return false;
        }
        return true;
    }
}


