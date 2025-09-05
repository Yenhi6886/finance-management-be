package com.example.backend.service;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.UserStatus;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng!");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActivationToken(UUID.randomUUID().toString());

        userRepository.save(user);

        // Gửi email kích hoạt
        emailService.sendActivationEmail(user.getEmail(), user.getActivationToken());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt!");
        }

        String token = jwtUtil.generateToken(authentication);
        UserResponse userResponse = userMapper.toUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    public void activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Token kích hoạt không hợp lệ!"));

        user.setStatus(UserStatus.ACTIVE);
        user.setActivationToken(null);
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(LocalDateTime.now().plusHours(1)); // Token hết hạn sau 1 giờ

        userRepository.save(user);

        // Gửi email reset password
        emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token reset password không hợp lệ!"));

        if (user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token reset password đã hết hạn!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);

        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();
        userMapper.updateUserFromRequest(request, currentUser);
        User updatedUser = userRepository.save(currentUser);
        return userMapper.toUserResponse(updatedUser);
    }

    public void deleteAccount() {
        User currentUser = getCurrentUser();
        currentUser.setStatus(UserStatus.DELETED);
        userRepository.save(currentUser);
    }

    public UserResponse getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        return userMapper.toUserResponse(currentUser);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
