package com.example.backend.security;

import com.example.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final transient User user;

    @Getter
    private final Long id;

    @Getter
    private Map<String, Object> attributes;

    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
    }

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.id = user.getId();
        this.attributes = attributes;
    }

    public static CustomUserDetails create(User user) {
        return new CustomUserDetails(user);
    }

    public static CustomUserDetails create(User user, Map<String, Object> attributes) {
        return new CustomUserDetails(user, attributes);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getName() {
        // For OAuth2 users, this is typically the name provided by the OAuth2 provider.
        // For local users, we can return email or username.
        // Let's return email for consistency with getUsername for now.
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus().name().equals("ACTIVE");
    }

    public User getUser() {
        return user;
    }
}