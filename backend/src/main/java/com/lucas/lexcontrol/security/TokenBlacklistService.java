package com.lucas.lexcontrol.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.Instant;

/**
 * Service to manage JWT token blacklist for logout functionality.
 * Tokens are stored with their expiration time and automatically removed after expiration.
 */
@ApplicationScoped
public class TokenBlacklistService {
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist with its expiration time
     * @param token JWT token to blacklist
     * @param expiresAt Unix timestamp when token expires
     */
    public void blacklistToken(String token, long expiresAt) {
        blacklist.put(token, expiresAt);
        // Clean up expired entries periodically
        cleanupExpiredTokens();
    }

    /**
     * Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted and not yet expired
     */
    public boolean isBlacklisted(String token) {
        Long expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }

        // Check if token is still valid (not yet expired)
        long currentTime = Instant.now().getEpochSecond();
        if (currentTime > expiresAt) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }

    /**
     * Remove all expired tokens from the blacklist
     */
    private void cleanupExpiredTokens() {
        long currentTime = Instant.now().getEpochSecond();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }

    /**
     * Clear all tokens from blacklist (for testing purposes)
     */
    public void clearAll() {
        blacklist.clear();
    }

    /**
     * Get the size of the blacklist
     */
    public int size() {
        return blacklist.size();
    }
}
