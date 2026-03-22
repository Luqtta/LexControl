package com.lucas.lexcontrol.dto.auth;

/**
 * Response for refresh token request
 */
public record RefreshTokenRequest(
        String refreshToken
) {
}
