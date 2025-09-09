package com.example.backend.security.oauth2;

import com.example.backend.enums.AuthProvider;
import com.example.backend.security.oauth2.user.GoogleOAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.FACEBOOK.toString())) {
            // return new FacebookOAuth2UserInfo(attributes);
            throw new UnsupportedOperationException("Facebook login not yet implemented.");
        } else if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
            // return new GithubOAuth2UserInfo(attributes);
            throw new UnsupportedOperationException("Github login not yet implemented.");
        } else {
            throw new UnsupportedOperationException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
