package com.example.backend;

import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testUser");
        userResponse.setEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testGetCurrentUserProfileSuccess() throws Exception {
        when(userService.getCurrentUserProfile()).thenReturn(userResponse);

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lấy thông tin profile thành công!"))
                .andExpect(jsonPath("$.data.username").value("testUser"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdateProfileSuccess() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("123456789");

        when(userService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật profile thành công!"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdateProfileFailure() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();

        when(userService.updateProfile(any(UpdateProfileRequest.class)))
                .thenThrow(new RuntimeException("Cập nhật thất bại"));

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cập nhật thất bại"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdateAvatarSuccess() throws Exception {
        when(userService.updateAvatar(any(MultipartFile.class))).thenReturn(userResponse);

        mockMvc.perform(multipart("/api/user/avatar")
                        .file("avatar", "fakeImageContent".getBytes()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật ảnh đại diện thành công!"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("123456");
        request.setNewPassword("newPassword");

        mockMvc.perform(post("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công!"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testChangePasswordFailure() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newPassword");

        doThrow(new RuntimeException("Sai mật khẩu cũ"))
                .when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sai mật khẩu cũ"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeleteAccountSuccess() throws Exception {
        mockMvc.perform(delete("/api/user/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa tài khoản thành công!"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeleteAccountFailure() throws Exception {
        doThrow(new RuntimeException("Không thể xóa tài khoản"))
                .when(userService).deleteAccount();

        mockMvc.perform(delete("/api/user/account"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Không thể xóa tài khoản"));
    }
}
