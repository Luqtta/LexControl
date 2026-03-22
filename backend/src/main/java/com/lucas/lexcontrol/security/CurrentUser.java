package com.lucas.lexcontrol.security;

import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class CurrentUser {

    @Inject
    JsonWebToken jwt;

    public UUID getUserId() {
        Object claim = jwt.getClaim("userId");
        if (claim == null) {
            return UUID.fromString(jwt.getSubject());
        }
        return UUID.fromString(claim.toString());
    }

    public String getEmail() {
        return jwt.getName();
    }
}
