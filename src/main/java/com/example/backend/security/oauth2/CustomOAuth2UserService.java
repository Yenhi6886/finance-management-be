package com.example.backend.security.oauth2;

import com.example.backend.entity.SocialAccount;
import com.example.backend.entity.User;
import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.UserStatus;
import com.example.backend.exception.OAuth2AuthenticationProcessingException;
import com.example.backend.repository.SocialAccountRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.security.oauth2.userinfo.OAuth2UserInfo;
import com.example.backend.security.oauth2.userinfo.OAuth2UserInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
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
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        String providerId = oAuth2UserInfo.getId();
        Optional<SocialAccount> socialAccountOptional = socialAccountRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (socialAccountOptional.isPresent()) {
            // User has logged in with this social account before
            user = socialAccountOptional.get().getUser();
        } else {
            // Social account not found, check if user with this email already exists
            Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
            if (userOptional.isPresent()) {
                // User with this email exists, link the new social account
                user = userOptional.get();
            } else {
                // A completely new user, register them
                user = registerNewUser(oAuth2UserInfo);
            }
            // Link the user to the social account
            linkSocialAccount(user, provider, providerId);
        }

        // Update user's info from social provider
        user = updateExistingUser(user, oAuth2UserInfo);

        return CustomUserDetails.create(user, oAuth2User.getAttributes());

    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setFullName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setStatus(UserStatus.ACTIVE); // Automatically active for social logins

        // Generate a unique username
        String email = oAuth2UserInfo.getEmail();
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }
        user.setUsername(username);

        // Generate a random password for social users
        String randomPassword = UUID.randomUUID().toString().substring(0, 16);
        user.setPassword(passwordEncoder.encode(randomPassword));

        return userRepository.save(user);
    }

    private void linkSocialAccount(User user, AuthProvider provider, String providerId) {
        SocialAccount socialAccount = new SocialAccount();
        socialAccount.setUser(user);
        socialAccount.setProvider(provider);
        socialAccount.setProviderId(providerId);
        socialAccountRepository.save(socialAccount);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // Update details that might change, like name or picture
        existingUser.setFullName(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }

}
