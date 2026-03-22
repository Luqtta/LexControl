package com.lucas.lexcontrol.controllers;

import com.lucas.lexcontrol.dto.auth.LoginResponse;
import com.lucas.lexcontrol.dto.auth.GenericResponse;
import com.lucas.lexcontrol.dto.auth.LoginRequest;
import com.lucas.lexcontrol.dto.auth.RegisterRequest;
import com.lucas.lexcontrol.services.AuthService;
import com.lucas.lexcontrol.services.TokenService;
import com.lucas.lexcontrol.security.TokenBlacklistService;
import com.lucas.lexcontrol.entities.User;
import com.lucas.lexcontrol.repositories.UserRepository;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.CookieParam;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.UUID;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    TokenService tokenService;

    @Inject
    TokenBlacklistService tokenBlacklistService;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "jwt.cookie.secure", defaultValue = "true")
    boolean cookieSecure;

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterRequest request) {
        authService.register(request);
        return Response.ok(new GenericResponse(
                "Account created. Please login."
        )).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        // Authenticate user (validates credentials) then generate ONE token pair
        User user = authService.authenticate(request);
        TokenService.TokenPair tokens = tokenService.generateTokens(user);

        LoginResponse loginResponse = new LoginResponse(
                tokens.accessToken,
                tokens.accessTokenExpiresAt,
                "Bearer",
                new com.lucas.lexcontrol.dto.auth.UserResponse(user.id, user.name, user.email, user.createdAt)
        );

        NewCookie refreshTokenCookie = buildRefreshCookie(tokens.refreshToken);

        return Response
                .ok(loginResponse)
                .cookie(refreshTokenCookie)
                .build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed("user")
    public Response logout() {
        if (jwt != null && jwt.getRawToken() != null) {
            // Blacklist the access token
            tokenBlacklistService.blacklistToken(jwt.getRawToken(), jwt.getExpirationTime());
            // Revoke all refresh tokens for this user
            try {
                UUID userId = UUID.fromString(jwt.getSubject());
                tokenService.revokeAllUserTokens(userId);
            } catch (Exception ignored) {
                // If we can't parse userId, access token blacklist is sufficient
            }
        }
        // Clear the refresh token cookie
        NewCookie expiredCookie = new NewCookie.Builder("refreshToken")
                .value("")
                .maxAge(0)
                .path("/auth")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
        return Response.ok(new GenericResponse("Logged out successfully")).cookie(expiredCookie).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public Response refresh(@CookieParam("refreshToken") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new GenericResponse("Refresh token is required"))
                    .build();
        }

        try {
            // Look up the userId from the stored refresh token map
            UUID userId = tokenService.getUserIdFromRefreshToken(refreshToken)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

            User user = userRepository.find("id", userId).firstResult();
            if (user == null) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(new GenericResponse("Invalid refresh token"))
                        .build();
            }

            // Generate new tokens
            TokenService.TokenPair tokens = tokenService.refreshAccessToken(refreshToken, user);

            LoginResponse response = new LoginResponse(
                    tokens.accessToken,
                    tokens.accessTokenExpiresAt,
                    "Bearer",
                    new com.lucas.lexcontrol.dto.auth.UserResponse(user.id, user.name, user.email, user.createdAt)
            );

            return Response
                    .ok(response)
                    .cookie(buildRefreshCookie(tokens.refreshToken))
                    .build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new GenericResponse("Invalid refresh token"))
                    .build();
        }
    }

    private NewCookie buildRefreshCookie(String value) {
        int maxAge = 7 * 24 * 60 * 60; // 7 days
        return new NewCookie.Builder("refreshToken")
                .value(value)
                .maxAge(maxAge)
                .path("/auth")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }
}
