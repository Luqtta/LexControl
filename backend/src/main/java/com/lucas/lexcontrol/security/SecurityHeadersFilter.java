package com.lucas.lexcontrol.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().putSingle("X-Frame-Options", "DENY");
        responseContext.getHeaders().putSingle("Referrer-Policy", "no-referrer");
        responseContext.getHeaders().putSingle("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        responseContext.getHeaders().putSingle("Content-Security-Policy", "default-src 'none'");
        responseContext.getHeaders().putSingle("Cache-Control", "no-store");

        String forwardedProto = requestContext.getHeaderString("X-Forwarded-Proto");
        if ("https".equalsIgnoreCase(forwardedProto) || requestContext.getUriInfo().getRequestUri().getScheme().equalsIgnoreCase("https")) {
            responseContext.getHeaders().putSingle("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
    }
}
