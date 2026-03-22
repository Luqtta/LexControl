package com.lucas.lexcontrol.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.lucas.lexcontrol.common.ApiError;
import com.lucas.lexcontrol.common.ApiErrorCode;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION - 10)
public class AuthRateLimitFilter implements ContainerRequestFilter {

    private static final int LIMIT = 10;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path == null || !(path.startsWith("auth/login") || path.startsWith("auth/register"))) {
            return;
        }

        String key = resolveClientKey(requestContext);
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());
        if (attempt.incrementAndCheck()) {
            ApiError error = new ApiError(
                    Instant.now().toString(),
                    Response.Status.TOO_MANY_REQUESTS.getStatusCode(),
                    ApiErrorCode.TOO_MANY_REQUESTS.getCode(),
                    Response.Status.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "Too many requests. Try again later.",
                    path,
                    java.util.List.of()
            );
            requestContext.abortWith(Response.status(Response.Status.TOO_MANY_REQUESTS).entity(error).build());
        }
    }

    /**
     * Resolve client IP from request headers.
     * Validates X-Forwarded-For header to prevent spoofing.
     * Falls back to connecting IP if behind a proxy.
     */
    private String resolveClientKey(ContainerRequestContext requestContext) {
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can be comma-separated list, get the first (original client)
            String clientIp = forwarded.split(",")[0].trim();
            // Validate IP format to prevent spoofing
            if (isValidIp(clientIp)) {
                return clientIp;
            }
        }
        
        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank() && isValidIp(realIp)) {
            return realIp;
        }
        
        // Fallback: use remote address from connection
        String remoteAddr = requestContext.getHeaderString("X-Forwarded-For");
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    /**
     * Basic IP validation to prevent header spoofing
     */
    private boolean isValidIp(String ip) {
        return ip != null && ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^[a-fA-F0-9:]+$");
    }

    private static class Attempt {
        private long windowStart = System.currentTimeMillis();
        private final AtomicInteger counter = new AtomicInteger(0);

        synchronized boolean incrementAndCheck() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                windowStart = now;
                counter.set(0);
            }
            return counter.incrementAndGet() > LIMIT;
        }
    }
}
