package com.complaintiq.auth;
import com.complaintiq.auth.enums.UserRole;
import com.complaintiq.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
@Slf4j @Component
public class JwtUtil {
    @Value("${app.jwt.secret}") private String jwtSecret;
    @Value("${app.jwt.access-token-expiry-ms}") private long accessTokenExpiryMs;
    public String generateAccessToken(String email, UserRole role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name()); claims.put("userId", userId);
        return buildToken(claims, email, accessTokenExpiryMs);
    }
    private String buildToken(Map<String,Object> extraClaims, String subject, long expiry) {
        return Jwts.builder().setClaims(extraClaims).setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiry))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }
    public boolean isTokenValid(String token, String email) {
        try { return extractEmail(token).equals(email) && !isTokenExpired(token); }
        catch (JwtException ex) { return false; }
    }
    public boolean validateToken(String token) {
        try { Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token); return true; }
        catch (ExpiredJwtException ex) { throw new InvalidTokenException("Token has expired."); }
        catch (UnsupportedJwtException ex) { throw new InvalidTokenException("Token format is unsupported."); }
        catch (MalformedJwtException ex) { throw new InvalidTokenException("Token is malformed."); }
        catch (SignatureException ex) { throw new InvalidTokenException("Token signature is invalid."); }
        catch (IllegalArgumentException ex) { throw new InvalidTokenException("Token is empty or null."); }
    }
    public String extractEmail(String token) { return extractClaim(token, Claims::getSubject); }
    public UserRole extractRole(String token) { return UserRole.valueOf(extractClaim(token, claims -> claims.get("role", String.class))); }
    public Long extractUserId(String token) { return extractClaim(token, claims -> claims.get("userId", Long.class)); }
    public Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }
    public long getAccessTokenExpiryMs() { return accessTokenExpiryMs; }
    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver) { return claimsResolver.apply(extractAllClaims(token)); }
    private boolean isTokenExpired(String token) { return extractExpiration(token).before(new Date()); }
    private Claims extractAllClaims(String token) {
        try { return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody(); }
        catch (ExpiredJwtException ex) { throw new InvalidTokenException("Token has expired."); }
        catch (JwtException ex) { throw new InvalidTokenException("Token is invalid."); }
    }
    private Key getSigningKey() { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)); }
}
