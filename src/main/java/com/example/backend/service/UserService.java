package com.example.backend.service;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.UserSettings;
import com.example.backend.entity.VerificationToken;
import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.UserStatus;
import com.example.backend.enums.VerificationTokenType;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserSettingsRepository;
import com.example.backend.repository.VerificationTokenRepository;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.filestorage.FileStorageService;
import com.example.backend.util.JwtUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.Optional;


@Service
@Transactional
public class UserService {
    private static final int TOKEN_EXPIRATION_IN_MINUTES = 15;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
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
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private WebClient.Builder webClientBuilder;

    // Lấy giá trị từ application.properties
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${app.oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    // ... (Các phương thức cũ như register, login, activateAccount, etc. giữ nguyên)
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng!");
        }
        if (StringUtils.hasText(request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.INACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        User savedUser = userRepository.save(user);

        // Create and send activation token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser, VerificationTokenType.ACCOUNT_ACTIVATION, TOKEN_EXPIRATION_IN_MINUTES * 4); // 1 hour for activation
        verificationTokenRepository.save(verificationToken);

        emailService.sendActivationEmail(user.getEmail(), token);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng sau khi xác thực: " + userDetails.getUsername()));


        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Tài khoản của bạn chưa được kích hoạt. Vui lòng kiểm tra email.");
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("Tài khoản của bạn đã bị vô hiệu hoá.");
        }

        String token = jwtUtil.generateToken(authentication);
        UserResponse userResponse = userMapper.toUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    public void activateAccount(String token) {
        VerificationToken verificationToken = validateToken(token, VerificationTokenType.ACCOUNT_ACTIVATION);

        User user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, user, VerificationTokenType.PASSWORD_RESET, TOKEN_EXPIRATION_IN_MINUTES);
            verificationTokenRepository.save(verificationToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    public void validatePasswordResetToken(String token) {
        validateToken(token, VerificationTokenType.PASSWORD_RESET);
    }

    public void resetPassword(String token, String newPassword) {
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số.");
        }
        VerificationToken verificationToken = validateToken(token, VerificationTokenType.PASSWORD_RESET);
        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
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

    public UserResponse updateAvatar(MultipartFile file) {
        User currentUser = getCurrentUser();
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/avatars/")
                .path(fileName)
                .toUriString();

        currentUser.setAvatarUrl(fileDownloadUri);
        User updatedUser = userRepository.save(currentUser);
        return userMapper.toUserResponse(updatedUser);
    }

    public UserResponse getCurrentUserProfile() {
        return userMapper.toUserResponse(getCurrentUser());
    }

    public void deleteAccount() {
        User currentUser = getCurrentUser();
        currentUser.setStatus(UserStatus.DELETED);
        userRepository.save(currentUser);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException("Yêu cầu xác thực để thực hiện hành động này.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng đã được xác thực trong database: " + userDetails.getUsername()));
    }

    private VerificationToken validateToken(String token, VerificationTokenType type) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ."));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Token đã được sử dụng.");
        }
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn.");
        }
        if (verificationToken.getType() != type) {
            throw new RuntimeException("Token không đúng loại.");
        }
        return verificationToken;
    }

    // --- CÁC PHƯƠNG THỨC MỚI CHO LUỒNG GOOGLE LOGIN ĐƠN GIẢN ---

    public String getGoogleOAuth2Url() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth";
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("access_type", "offline")
                .toUriString();
    }

    public String processGoogleCallback(String code) {
        // 1. Dùng code để lấy access_token từ Google
        GoogleTokenResponse tokenResponse = getGoogleToken(code);

        // 2. Dùng access_token để lấy thông tin người dùng từ Google
        GoogleUserInfoResponse userInfo = getGoogleUserInfo(tokenResponse.getAccessToken());

        // 3. Tìm hoặc tạo người dùng mới trong database
        User user = findOrCreateUser(userInfo);

        // 4. Tạo JWT token của hệ thống và trả về
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                CustomUserDetails.create(user), null, CustomUserDetails.create(user).getAuthorities());
        return jwtUtil.generateToken(authentication);
    }

    private GoogleTokenResponse getGoogleToken(String code) {
        WebClient webClient = webClientBuilder.baseUrl("https://oauth2.googleapis.com").build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleClientId);
        formData.add("client_secret", googleClientSecret);
        formData.add("redirect_uri", googleRedirectUri);
        formData.add("grant_type", "authorization_code");

        return webClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();
    }

    private GoogleUserInfoResponse getGoogleUserInfo(String accessToken) {
        WebClient webClient = webClientBuilder.baseUrl("https://www.googleapis.com").build();
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth2/v3/userinfo").build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserInfoResponse.class)
                .block();
    }

    private User findOrCreateUser(GoogleUserInfoResponse userInfo) {
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            // Cập nhật thông tin nếu cần
            existingUser.setFirstName(userInfo.getGivenName());
            existingUser.setLastName(userInfo.getFamilyName());
            existingUser.setAvatarUrl(userInfo.getPicture());
            return userRepository.save(existingUser);
        } else {
            // Tạo user mới
            User newUser = new User();
            newUser.setEmail(userInfo.getEmail());
            newUser.setFirstName(userInfo.getGivenName());
            newUser.setLastName(userInfo.getFamilyName());
            newUser.setAvatarUrl(userInfo.getPicture());
            newUser.setUsername("google_" + userInfo.getSub()); // Tạo username duy nhất
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Mật khẩu ngẫu nhiên
            newUser.setAuthProvider(AuthProvider.GOOGLE);
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setProviderId(userInfo.getSub());

            User savedUser = userRepository.save(newUser);

            // Tạo UserSettings mặc định
            UserSettings userSettings = new UserSettings();
            userSettings.setUser(savedUser);
            userSettingsRepository.save(userSettings);

            return savedUser;
        }
    }

    // Lớp nội tại để chứa response từ Google
    @Data
    private static class GoogleTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("expires_in")
        private Integer expiresIn;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("scope")
        private String scope;
        @JsonProperty("id_token")
        private String idToken;
    }

    @Data
    private static class GoogleUserInfoResponse {
        private String sub;
        private String name;
        @JsonProperty("given_name")
        private String givenName;
        @JsonProperty("family_name")
        private String familyName;
        private String picture;
        private String email;
        @JsonProperty("email_verified")
        private Boolean emailVerified;
        private String locale;
    }
}