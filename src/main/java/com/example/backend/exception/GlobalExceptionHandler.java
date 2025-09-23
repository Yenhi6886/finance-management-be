package com.example.backend.exception;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageService messageService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String messageKey = error.getDefaultMessage();
            errors.put(fieldName, messageKey);
        });

        String message = messageService.getMessage("validation.invalid.data");
        ApiResponse<Map<String, String>> response = new ApiResponse<>(false, message, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        String message = messageService.getMessage(ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        String message = messageService.getMessage(ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        String message = messageService.getMessage(ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String message = messageService.getMessage(ex.getMessage(), ex.getArgs());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        String message = messageService.getMessage(ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        String message = messageService.getMessage(ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleWalletNotFoundException(WalletNotFoundException ex) {
        String message = messageService.getMessage(ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OAuth2AccountConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleOAuth2AccountConflictException(OAuth2AccountConflictException ex) {
        String message = messageService.getMessage("auth.oauth2.account.conflict",
                new Object[]{ex.getEmail(), ex.getExistingProvider(), ex.getAttemptedProvider()});
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String message = messageService.getMessage("exception.internal.server.error");
        ApiResponse<Void> response = new ApiResponse<>(false, message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}