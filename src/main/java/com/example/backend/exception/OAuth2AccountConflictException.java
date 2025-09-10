package com.example.backend.exception;

import org.springframework.security.core.AuthenticationException;

public class OAuth2AccountConflictException extends AuthenticationException {

    private String existingProvider;
    private String attemptedProvider;
    private String email;

    public OAuth2AccountConflictException(String email, String existingProvider, String attemptedProvider) {
        super(String.format("Account with email %s already exists with %s provider. Cannot login with %s.",
                email, existingProvider, attemptedProvider));
        this.email = email;
        this.existingProvider = existingProvider;
        this.attemptedProvider = attemptedProvider;
    }

    public String getExistingProvider() {
        return existingProvider;
    }

    public String getAttemptedProvider() {
        return attemptedProvider;
    }

    public String getEmail() {
        return email;
    }
}
