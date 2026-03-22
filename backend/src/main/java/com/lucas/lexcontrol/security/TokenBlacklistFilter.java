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
import java.util.List;

/**
 * Filter to check if JWT token has been blacklisted (revoked).
 * This runs after JWT validation, checking if the token was previously logged out.
 */
@Provider
public class TokenBlacklistFilter implements ContainerRequestFilter {
    @Inject
    TokenBlacklistService blacklistService;

    @Inject
    JsonWebToken jwt;

    @Context
    UriInfo uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip filtering for public endpoints (login, register, health checks)
        String path = requestContext.getUriInfo().getPath();
        if (isPublicEndpoint(path)) {
            return;
        }

        // Skip if no JWT token is present
        if (jwt == null || jwt.getRawToken() == null) {
            return;
        }

        // Check if token is blacklisted
        if (blacklistService.isBlacklisted(jwt.getRawToken())) {
            ApiError error = new ApiError(
                    Instant.now().toString(),
                    401,
                    ApiErrorCode.INVALID_TOKEN.getCode(),
                    "Unauthorized",
                    "Token has been revoked. Please login again.",
                    uriInfo != null ? uriInfo.getPath() : null,
                    List.of()
            );
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build()
            );
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/auth/login") ||
               path.contains("/auth/register") ||
               path.contains("/q/health") ||
               path.contains("/q/health/live") ||
               path.contains("/q/health/ready") ||
               path.startsWith("/openapi");
    }
}
