package com.lucas.lexcontrol.security;

import com.lucas.lexcontrol.services.TokenService;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Periodic backstop that forces eviction of expired refresh tokens and blacklisted
 * tokens. Caffeine already evicts entries on access and during its own maintenance,
 * but a fixed-interval sweep guarantees stale entries are dropped even when the
 * caches are otherwise idle (e.g. after a quiet period following a restart).
 */
@ApplicationScoped
public class TokenCleanupScheduler {

    @Inject
    TokenService tokenService;

    @Inject
    TokenBlacklistService tokenBlacklistService;

    @Scheduled(every = "5m")
    void cleanupExpiredTokens() {
        tokenService.purgeExpired();
        tokenBlacklistService.purgeExpired();
    }
}
