package com.lucas.lexcontrol.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import com.lucas.lexcontrol.common.ApiError;
import com.lucas.lexcontrol.common.ApiErrorCode;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

/**
 * Rate limiting filter for data endpoints (clients, transactions).
 * Limits requests per authenticated user to prevent abuse.
 */
@Provider
public class DataEndpointRateLimitFilter implements ContainerRequestFilter {
    private static final long RESET_WINDOW_MS = 60000; // 1 minute
    private static final int LIMIT_PER_WINDOW = 100; // 100 requests per minute

    private final Map<UUID, RateLimitData> rateLimitMap = new ConcurrentHashMap<>();

    @Inject
    JsonWebToken jwt;

    @Context
    UriInfo uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Apply rate limiting only to data endpoints
        if (!isDataEndpoint(path)) {
            return;
        }

        // Skip rate limiting if no JWT token is present
        if (jwt == null || jwt.getSubject() == null) {
            return;
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        long now = System.currentTimeMillis();

        RateLimitData data = rateLimitMap.computeIfAbsent(userId, k -> new RateLimitData());

        // Reset counter if window has passed
        if (now - data.windowStart > RESET_WINDOW_MS) {
            data.windowStart = now;
            data.requestCount = 0;
        }

        data.requestCount++;

        if (data.requestCount > LIMIT_PER_WINDOW) {
            ApiError error = new ApiError(
                    Instant.now().toString(),
                    429,
                    ApiErrorCode.TOO_MANY_REQUESTS.getCode(),
                    "Too Many Requests",
                    "Rate limit exceeded. Maximum 100 requests per minute.",
                    uriInfo != null ? uriInfo.getPath() : null,
                    List.of()
            );
            requestContext.abortWith(
                Response.status(429)
                    .entity(error)
                    .build()
            );
        }

        // Cleanup old entries periodically
        if (rateLimitMap.size() > 10000) {
            cleanupExpired(now);
        }
    }

    private boolean isDataEndpoint(String path) {
        return path.contains("/client") ||
               path.contains("/transaction") ||
               path.contains("/dashboard");
    }

    private void cleanupExpired(long now) {
        rateLimitMap.entrySet().removeIf(entry ->
            now - entry.getValue().windowStart > RESET_WINDOW_MS * 2
        );
    }

    private static class RateLimitData {
        long windowStart = System.currentTimeMillis();
        int requestCount = 0;
    }
}
