package com.example.backend.util;

import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.MessageService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private MessageService messageService;

    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        String usernameToUse;

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            usernameToUse = userDetails.getUsername();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            // For OAuth2 users, we use their email as the subject for consistency
            usernameToUse = oauth2User.getAttribute("email");
        } else {
            throw new IllegalArgumentException("Unsupported principal type: " + principal.getClass().getName());
        }
        return generateTokenFromUsername(usernameToUse);
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("{}: {}", messageService.getMessage("jwt.invalid.token"), e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("{}: {}", messageService.getMessage("jwt.expired.token"), e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("{}: {}", messageService.getMessage("jwt.unsupported.token"), e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("{}: {}", messageService.getMessage("jwt.empty.claims"), e.getMessage());
        }
        return false;
    }
}
