package com.lucas.lexcontrol.services;

import com.lucas.lexcontrol.entities.User;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Set;

/**
 * Service to generate and manage JWT tokens (access and refresh tokens)
 */
@ApplicationScoped
public class TokenService {
    @ConfigProperty(name = "jwt.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.expiration")
    long accessTokenExpiration;

    @ConfigProperty(name = "jwt.refresh.expiration")
    long refreshTokenExpiration;

    // In-memory storage for refresh tokens, bounded and auto-evicting via Caffeine.
    // Entries expire 7 days after write (matching refresh token TTL) and the cache is
    // capped at 10k entries. A scheduled sweep (TokenCleanupScheduler) is a backstop.
    private final Cache<String, RefreshTokenData> refreshTokens = Caffeine.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .maximumSize(10_000)
            .build();

    /**
     * Generate both access token and refresh token for a user
     */
    public TokenPair generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        long now = Instant.now().getEpochSecond();
        long accessTokenExpiresAt = now + accessTokenExpiration;
        long refreshTokenExpiresAt = now + refreshTokenExpiration;

        // Store refresh token
        refreshTokens.put(refreshToken, new RefreshTokenData(
                user.id,
                refreshTokenExpiresAt
        ));

        return new TokenPair(
                accessToken,
                refreshToken,
                accessTokenExpiresAt,
                refreshTokenExpiresAt
        );
    }

    /**
     * Generate a new access token from a valid refresh token
     */
    public TokenPair refreshAccessToken(String refreshToken, User user) {
        if (!isValidRefreshToken(refreshToken, user.id)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Revoke old refresh token
        refreshTokens.invalidate(refreshToken);

        // Generate new tokens
        return generateTokens(user);
    }

    /**
     * Validate refresh token
     */
    public boolean isValidRefreshToken(String refreshToken, UUID userId) {
        RefreshTokenData data = refreshTokens.getIfPresent(refreshToken);
        if (data == null) {
            return false;
        }

        // Check if token is expired
        long currentTime = Instant.now().getEpochSecond();
        if (currentTime > data.expiresAt) {
            refreshTokens.invalidate(refreshToken);
            return false;
        }

        // Check if token belongs to the user
        return data.userId.equals(userId);
    }

    /**
     * Validate refresh token and return the associated userId
     */
    public java.util.Optional<UUID> getUserIdFromRefreshToken(String refreshToken) {
        RefreshTokenData data = refreshTokens.getIfPresent(refreshToken);
        if (data == null) {
            return java.util.Optional.empty();
        }
        long currentTime = Instant.now().getEpochSecond();
        if (currentTime > data.expiresAt) {
            refreshTokens.invalidate(refreshToken);
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(data.userId);
    }

    /**
     * Revoke a refresh token
     */
    public void revokeRefreshToken(String refreshToken) {
        refreshTokens.invalidate(refreshToken);
    }

    /**
     * Revoke all refresh tokens for a user (logout from all devices)
     */
    public void revokeAllUserTokens(UUID userId) {
        refreshTokens.asMap().values().removeIf(data -> data.userId.equals(userId));
    }

    /**
     * Backstop sweep for logically-expired refresh tokens. Caffeine already evicts
     * entries 7 days after write, but this runs Caffeine's maintenance and drops any
     * token already past its own expiry. Invoked by {@code TokenCleanupScheduler}.
     */
    public void purgeExpired() {
        long now = Instant.now().getEpochSecond();
        refreshTokens.asMap().values().removeIf(data -> now > data.expiresAt);
        refreshTokens.cleanUp();
    }

    private String generateAccessToken(User user) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + accessTokenExpiration;

        return Jwt.issuer(issuer)
                .subject(user.id.toString())
                .groups(Set.of("user"))
                .expiresAt(expiresAt)
                .issuedAt(issuedAt)
                .claim("email", user.email)
                .claim("name", user.name)
                .sign();
    }

    private String generateRefreshToken(User user) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + refreshTokenExpiration;

        return Jwt.issuer(issuer)
                .subject(user.id.toString())
                .expiresAt(expiresAt)
                .issuedAt(issuedAt)
                .claim("type", "refresh")
                .sign();
    }

    /**
     * Data class for stored refresh tokens
     */
    private static class RefreshTokenData {
        UUID userId;
        long expiresAt;

        RefreshTokenData(UUID userId, long expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * Response with both access and refresh tokens
     */
    public static class TokenPair {
        public String accessToken;
        public String refreshToken;
        public long accessTokenExpiresAt;
        public long refreshTokenExpiresAt;

        public TokenPair(String accessToken, String refreshToken, long accessTokenExpiresAt, long refreshTokenExpiresAt) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessTokenExpiresAt = accessTokenExpiresAt;
            this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        }
    }
}
