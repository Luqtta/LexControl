package com.lucas.lexcontrol.dto.auth;

/**
 * Response for login endpoint containing access token details
 * Refresh token is sent as HttpOnly cookie, not in response body
 */
public record LoginResponse(
        String accessToken,
        long expiresAt,
        String tokenType,
        UserResponse user
) {
}
