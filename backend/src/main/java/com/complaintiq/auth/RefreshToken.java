package com.complaintiq.auth;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="refresh_tokens",indexes={
    @Index(name="idx_refresh_token_value",columnList="token"),
    @Index(name="idx_refresh_token_user_id",columnList="user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=500) private String token;
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="user_email",nullable=false) private String userEmail;
    @Column(name="expires_at",nullable=false) private LocalDateTime expiresAt;
    @Column(name="revoked",nullable=false) @Builder.Default private Boolean revoked = false;
    @Column(name="created_at",nullable=false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isValid() { return !revoked && !isExpired(); }
}
