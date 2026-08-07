package com.oow.todowithspirit.domain.auth;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenProvider {

    private final SecretKey key;

    private static final long ACCESS_TOKEN_EXPIRE_MIN = 60; // 1-hour
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7; // 7-days

    public TokenProvider(@Value("${token.secret.key}") String tokenSecretKey) {
        this.key = Keys.hmacShaKeyFor(tokenSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create token
     */

    public String createAccessToken(User user) {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(ACCESS_TOKEN_EXPIRE_MIN);
        return createToken(user, expiry);
    }

    public String createRefreshToken(User user) {
        LocalDateTime expiry = LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS);
        return createToken(user, expiry);
    }

    private String createToken(User user, LocalDateTime expiry) {
        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("provider", user.getProvider())
                .claim("providerUserId", user.getProviderUserId())
                .claim("role", user.getRole())
                .claim("isPremium", user.isPremium())
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiration(Timestamp.valueOf(expiry))
                .signWith(key)
                .compact();
    }

    /**
     * Validte token
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Extract claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        Claims claims = getClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Boolean isPremium(String token) {
        return getClaims(token).get("isPremium", Boolean.class);
    }

    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }
}
