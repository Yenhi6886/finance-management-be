package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.enums.UserStatus;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user;
        if (identifier != null && identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tồn tại người dùng với email: " + identifier));
        } else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tồn tại người dùng với username: " + identifier));
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadCredentialsException("Tài khoản của bạn chưa được kích hoạt. Vui lòng kiểm tra email để kích hoạt.");
        }

        return new CustomUserDetails(user);
    }
}