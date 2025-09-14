
package com.example.backend.controller;

import com.example.backend.dto.request.DepositRequest;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.Currency;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import com.example.backend.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Wallet userWallet;
    private Authentication userAuthentication;
    private Authentication otherUserAuthentication;

    @BeforeEach
    void setUp() {
        // Tạo user chính
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user = userRepository.save(user);

        // Tạo ví cho user chính
        userWallet = new Wallet();
        userWallet.setName("Ví chính");
        userWallet.setIcon("icon");
        userWallet.setCurrency(Currency.VND);
        userWallet.setBalance(new BigDecimal("1000000"));
        userWallet.setUser(user);
        userWallet = walletRepository.save(userWallet);

        // Tạo đối tượng Authentication cho user chính
        CustomUserDetails userDetails = new CustomUserDetails(user);
        userAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Tạo user phụ (không có quyền)
        User otherUser = new User();
        otherUser.setEmail("otheruser@example.com");
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser = userRepository.save(otherUser);

        // Tạo đối tượng Authentication cho user phụ
        CustomUserDetails otherUserDetails = new CustomUserDetails(otherUser);
        otherUserAuthentication = new UsernamePasswordAuthenticationToken(otherUserDetails, null, otherUserDetails.getAuthorities());
        
        // Xóa context giữa các bài test
        SecurityContextHolder.clearContext();
    }

    @Test
    void deposit_whenSuccess_shouldReturn200AndTransactionResponse() throws Exception {
        // "Đăng nhập" với user chính
        SecurityContextHolder.getContext().setAuthentication(userAuthentication);

        DepositRequest request = new DepositRequest(userWallet.getId(), new BigDecimal("500000"), "Nạp tiền lương");

        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nạp tiền vào ví thành công"))
                .andExpect(jsonPath("$.data.amount").value(500000))
                .andExpect(jsonPath("$.data.type").value("INCOME"));

        // Kiểm tra số dư ví trong DB đã được cập nhật
        Wallet updatedWallet = walletRepository.findById(userWallet.getId()).get();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(new BigDecimal("1500000"));
    }

    @Test
    void deposit_whenAmountIsZero_shouldReturn400() throws Exception {
        // "Đăng nhập" với user chính
        SecurityContextHolder.getContext().setAuthentication(userAuthentication);

        DepositRequest request = new DepositRequest(userWallet.getId(), BigDecimal.ZERO, "Nạp tiền 0 đồng");

        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_whenUserHasNoPermission_shouldReturn403() throws Exception {
        // "Đăng nhập" với user phụ
        SecurityContextHolder.getContext().setAuthentication(otherUserAuthentication);

        // User phụ cố gắng nạp tiền vào ví của user chính
        DepositRequest request = new DepositRequest(userWallet.getId(), new BigDecimal("100000"), "Nạp tiền trái phép");

        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
