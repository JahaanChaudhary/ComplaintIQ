package com.complaintiq.auth;
import com.complaintiq.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
@Slf4j @Service @RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${app.jwt.refresh-token-expiry-days}") private int refreshTokenExpiryDays;
    @Transactional
    public RefreshToken createRefreshToken(Long userId, String userEmail) {
        refreshTokenRepository.revokeAllByUserId(userId);
        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).userId(userId).userEmail(userEmail).expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays)).revoked(false).build();
        return refreshTokenRepository.save(refreshToken);
    }
    @Transactional(readOnly=true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new InvalidTokenException("Refresh token not found."));
        if (!refreshToken.isValid()) {
            if (refreshToken.getRevoked()) throw new InvalidTokenException("Refresh token has been revoked.");
            throw new InvalidTokenException("Refresh token has expired.");
        }
        return refreshToken;
    }
    @Transactional public void revokeByUserId(Long userId) { refreshTokenRepository.revokeAllByUserId(userId); }
    @Transactional public void revokeToken(String token) { refreshTokenRepository.findByToken(token).ifPresent(rt -> { rt.setRevoked(true); refreshTokenRepository.save(rt); }); }
    @Scheduled(cron="0 0 2 * * *") @Transactional
    public void cleanupExpiredTokens() { refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now()); }
}
