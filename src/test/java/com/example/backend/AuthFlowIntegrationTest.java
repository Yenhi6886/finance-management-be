package com.example.backend;

import com.example.backend.entity.User;
import com.example.backend.entity.VerificationToken;
import com.example.backend.enums.UserStatus;
import com.example.backend.enums.VerificationTokenType;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VerificationTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        userRepository.save(testUser);
    }

    @Test
    void forgotPassword_shouldReturnGenericSuccessMessage_whenEmailExists() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Nếu email của bạn tồn tại trong hệ thống, một liên kết để đặt lại mật khẩu đã được gửi đến."));
    }

    @Test
    void forgotPassword_shouldReturnGenericSuccessMessage_whenEmailDoesNotExist() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Nếu email của bạn tồn tại trong hệ thống, một liên kết để đặt lại mật khẩu đã được gửi đến."));
    }

    @Test
    void validateResetToken_shouldSucceed_whenTokenIsValid() throws Exception {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, testUser, VerificationTokenType.PASSWORD_RESET, 15);
        verificationTokenRepository.save(verificationToken);

        mockMvc.perform(get("/api/auth/validate-reset-token").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token hợp lệ."));
    }

    @Test
    void validateResetToken_shouldFail_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/auth/validate-reset-token").param("token", "invalid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateResetToken_shouldFail_whenTokenIsExpired() throws Exception {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, testUser, VerificationTokenType.PASSWORD_RESET, -1); // Expired
        verificationTokenRepository.save(verificationToken);

        mockMvc.perform(get("/api/auth/validate-reset-token").param("token", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldSucceed_whenTokenIsValid() throws Exception {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, testUser, VerificationTokenType.PASSWORD_RESET, 15);
        verificationTokenRepository.save(verificationToken);

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mật khẩu đã được thay đổi thành công."));
    }

    @Test
    void resetPassword_shouldFail_whenPasswordIsWeak() throws Exception {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, testUser, VerificationTokenType.PASSWORD_RESET, 15);
        verificationTokenRepository.save(verificationToken);

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("newPassword", "weak");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
