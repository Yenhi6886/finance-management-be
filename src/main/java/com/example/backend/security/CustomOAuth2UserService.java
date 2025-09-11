package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.UserStatus;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.oauth2.OAuth2UserInfo;
import com.example.backend.security.oauth2.OAuth2UserInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional // Add @Transactional annotation
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Thay vì throw exception, cho phép merge account
            AuthProvider currentProvider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());

            // Nếu user đã tồn tại với provider khác, cập nhật provider về OAuth2
            if (!user.getAuthProvider().equals(currentProvider)) {
                // Cập nhật provider sang OAuth2 và lưu providerId
                user.setAuthProvider(currentProvider);
                user.setProviderId(oAuth2UserInfo.getId());

            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return CustomUserDetails.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setAuthProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setEmail(oAuth2UserInfo.getEmail());

        // Handle name mapping
        String fullName = oAuth2UserInfo.getName();
        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split(" ", 2); // Split by first space
            user.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            } else {
                user.setLastName(""); // Ensure it's not null if not provided
            }
        } else {
            user.setFirstName(""); // Ensure it's not null
            user.setLastName(""); // Ensure it's not null
        }

        user.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        user.setStatus(UserStatus.ACTIVE);

        // Handle password for OAuth2 users: set a random dummy password
        user.setPassword(UUID.randomUUID().toString());

        // Handle username for OAuth2 users: generate a unique username to avoid conflicts
        // Format: oauth2_<provider>_<providerId>
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId().toLowerCase();
        user.setUsername("oauth2_" + provider + "_" + oAuth2UserInfo.getId());

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // Update name if changed
        String fullName = oAuth2UserInfo.getName();
        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split(" ", 2);
            existingUser.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                existingUser.setLastName(nameParts[1]);
            } else {
                existingUser.setLastName("");
            }
        }
        existingUser.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
