package com.lucas.lexcontrol.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.TimeUnit;
import java.time.Instant;

/**
 * Service to manage JWT token blacklist for logout functionality.
 * Tokens are stored with their expiration time and automatically evicted by Caffeine
 * (24h after write, capped at 10k entries); no manual sweeping is required.
 */
@ApplicationScoped
public class TokenBlacklistService {
    private final Cache<String, Long> blacklist = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    /**
     * Add a token to the blacklist with its expiration time
     * @param token JWT token to blacklist
     * @param expiresAt Unix timestamp when token expires
     */
    public void blacklistToken(String token, long expiresAt) {
        blacklist.put(token, expiresAt);
    }

    /**
     * Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted and not yet expired
     */
    public boolean isBlacklisted(String token) {
        Long expiresAt = blacklist.getIfPresent(token);
        if (expiresAt == null) {
            return false;
        }

        // Check if token is still valid (not yet expired)
        long currentTime = Instant.now().getEpochSecond();
        if (currentTime > expiresAt) {
            blacklist.invalidate(token);
            return false;
        }

        return true;
    }

    /**
     * Run Caffeine's maintenance to evict expired entries. Eviction is automatic, but a
     * scheduled call ({@code TokenCleanupScheduler}) forces pending cleanup promptly.
     */
    public void purgeExpired() {
        blacklist.cleanUp();
    }

    /**
     * Clear all tokens from blacklist (for testing purposes)
     */
    public void clearAll() {
        blacklist.invalidateAll();
    }

    /**
     * Get the size of the blacklist
     */
    public int size() {
        return (int) blacklist.estimatedSize();
    }
}
