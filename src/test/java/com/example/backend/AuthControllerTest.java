package com.example.backend;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.security.CustomUserDetailsService;
import com.example.backend.service.JwtBlacklistService;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtBlacklistService jwtBlacklistService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setUsername("testUser");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setPhoneNumber("0123456789");

        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("test@example.com");
        loginRequest.setPassword("123456");

        forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("test@example.com");

        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("reset-token");
        resetPasswordRequest.setNewPassword("newPass123");
    }


    @Test
    void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản."));
    }

    @Test
    void testRegisterFailure() throws Exception {
        doThrow(new RuntimeException("Email đã tồn tại")).when(userService).register(any());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email đã tồn tại"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(userService.login(any())).thenReturn(new AuthResponse("token", "refresh", null));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công!"))
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    void testLoginFailure() throws Exception {
        doThrow(new RuntimeException("Sai mật khẩu")).when(userService).login(any());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sai mật khẩu"));
    }

    @Test
    void testActivateAccountSuccess() throws Exception {
        mockMvc.perform(get("/api/auth/activate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Kích hoạt tài khoản thành công!"));
    }

    @Test
    void testActivateAccountFailure() throws Exception {
        Mockito.doThrow(new RuntimeException("Token không hợp lệ"))
                .when(userService).activateAccount("invalid");

        mockMvc.perform(get("/api/auth/activate")
                        .param("token", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token không hợp lệ"));
    }

    @Test
    void testForgotPasswordSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Link đặt lại mật khẩu đã được gửi đến email của bạn."));
    }

    @Test
    void testForgotPasswordFailure() throws Exception {
        doThrow(new RuntimeException("Email không tồn tại")).when(userService).forgotPassword(any());
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email không tồn tại"));
    }

    @Test
    void testResetPasswordSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đặt lại mật khẩu thành công!"));
    }

    @Test
    void testResetPasswordFailure() throws Exception {
        doThrow(new RuntimeException("Token hết hạn")).when(userService).resetPassword(any());
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token hết hạn"));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        when(jwtUtil.getExpirationDateFromToken("valid-token"))
                .thenReturn(new Date(System.currentTimeMillis() + 10000));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công!"));
    }

    @Test
    void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công!"));
    }

    @Test
    void testLogoutFailure() throws Exception {
        when(jwtUtil.getExpirationDateFromToken("bad-token"))
                .thenThrow(new RuntimeException("Token lỗi"));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token lỗi"));
    }
}
