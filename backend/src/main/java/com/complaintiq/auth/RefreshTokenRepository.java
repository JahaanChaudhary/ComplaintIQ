package com.complaintiq.auth;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    boolean existsByToken(String token);
    @Modifying @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);
    @Modifying @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    void deleteExpiredAndRevoked(@Param("now") LocalDateTime now);
    @Query("SELECT r FROM RefreshToken r WHERE r.userId = :userId AND r.revoked = false AND r.expiresAt > :now")
    Optional<RefreshToken> findValidTokenByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
