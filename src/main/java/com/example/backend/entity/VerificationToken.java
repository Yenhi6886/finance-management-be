package com.example.backend.entity;

import com.example.backend.enums.VerificationTokenType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Data
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenType type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used = false;

    public VerificationToken(String token, User user, VerificationTokenType type, int expirationTimeInMinutes) {
        this.token = token;
        this.user = user;
        this.type = type;
        this.expiresAt = LocalDateTime.now().plusMinutes(expirationTimeInMinutes);
    }
}
