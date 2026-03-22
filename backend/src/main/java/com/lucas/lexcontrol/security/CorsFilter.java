package com.lucas.lexcontrol.security;

import java.util.Arrays;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CorsFilter {

    private static final List<String> FALLBACK_ORIGINS = List.of(
            "https://lex-control-eosin.vercel.app",
            "http://localhost:5173"
    );

    @ConfigProperty(name = "quarkus.http.cors.origins", defaultValue = "")
    String allowedOrigins;

    @ConfigProperty(name = "quarkus.http.cors.methods", defaultValue = "GET,POST,PUT,DELETE,OPTIONS")
    String allowedMethods;

    @ConfigProperty(name = "quarkus.http.cors.headers", defaultValue = "accept,authorization,content-type,x-requested-with")
    String allowedHeaders;

    @ConfigProperty(name = "quarkus.http.cors.exposed-headers", defaultValue = "location")
    String exposedHeaders;

    @ConfigProperty(name = "quarkus.http.cors.credentials", defaultValue = "false")
    boolean allowCredentials;

    @ConfigProperty(name = "quarkus.http.cors.access-control-max-age", defaultValue = "86400")
    String maxAge;

    @RouteFilter(10000)
    void filter(RoutingContext routingContext) {
        String origin = routingContext.request().getHeader("Origin");
        if (origin == null || !isAllowedOrigin(origin)) {
            routingContext.next();
            return;
        }

        routingContext.response().putHeader("Access-Control-Allow-Origin", origin);
        routingContext.response().putHeader("Vary", "Origin");
        routingContext.response().putHeader("Access-Control-Allow-Methods", allowedMethods);
        routingContext.response().putHeader("Access-Control-Allow-Headers", allowedHeaders);
        routingContext.response().putHeader("Access-Control-Expose-Headers", exposedHeaders);
        routingContext.response().putHeader("Access-Control-Max-Age", maxAge);
        if (allowCredentials) {
            routingContext.response().putHeader("Access-Control-Allow-Credentials", "true");
        }

        if (routingContext.request().method() == HttpMethod.OPTIONS) {
            routingContext.response().setStatusCode(204).end();
            return;
        }

        routingContext.next();
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }

        if (isFallbackOrigin(origin)) {
            return true;
        }

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return false;
        }

        String trimmed = stripWrappingQuotes(allowedOrigins);
        if (trimmed == null || trimmed.isBlank()) {
            return false;
        }
        if ("*".equals(trimmed)) {
            return true;
        }
        if (trimmed.startsWith("/") && trimmed.endsWith("/")) {
            String pattern = trimmed.substring(1, trimmed.length() - 1);
            return origin.matches(pattern);
        }

        List<String> origins = Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .map(CorsFilter::stripWrappingQuotes)
                .filter(value -> value != null && !value.isBlank())
                .toList();
        return origins.contains(origin) || isFallbackOrigin(origin);
    }

    private static String stripWrappingQuotes(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        boolean changed;
        do {
            changed = false;
            if (cleaned.startsWith("\\\"")) {
                cleaned = cleaned.substring(2).trim();
                changed = true;
            } else if (cleaned.startsWith("\\'")) {
                cleaned = cleaned.substring(2).trim();
                changed = true;
            } else if (cleaned.startsWith("\"")) {
                cleaned = cleaned.substring(1).trim();
                changed = true;
            } else if (cleaned.startsWith("'")) {
                cleaned = cleaned.substring(1).trim();
                changed = true;
            }

            if (cleaned.endsWith("\\\"")) {
                cleaned = cleaned.substring(0, cleaned.length() - 2).trim();
                changed = true;
            } else if (cleaned.endsWith("\\'")) {
                cleaned = cleaned.substring(0, cleaned.length() - 2).trim();
                changed = true;
            } else if (cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
                changed = true;
            } else if (cleaned.endsWith("'")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
                changed = true;
            }
        } while (changed && !cleaned.isBlank());
        return cleaned;
    }

    private static boolean isFallbackOrigin(String origin) {
        return FALLBACK_ORIGINS.contains(origin);
    }
}
