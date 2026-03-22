package com.lucas.lexcontrol.security;

import java.time.Instant;
import java.util.Set;

import com.lucas.lexcontrol.entities.User;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.expiration")
    long expirationSeconds;

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwt.issuer(issuer)
                .subject(user.id.toString())
                .upn(user.email)
                .groups(Set.of("user"))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("userId", user.id.toString())
                .claim("name", user.name)
                .sign(); 
    }

    public Instant getExpiryInstant() {
        return Instant.now().plusSeconds(expirationSeconds);
    }
}