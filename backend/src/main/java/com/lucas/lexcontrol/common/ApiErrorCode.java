package com.lucas.lexcontrol.common;

/**
 * Structured error codes for API responses.
 * Enables frontend to handle errors programmatically.
 */
public enum ApiErrorCode {
    // Auth errors
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", 401),
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED", 409),
    UNAUTHORIZED("UNAUTHORIZED", 401),
    FORBIDDEN("FORBIDDEN", 403),
    INVALID_TOKEN("INVALID_TOKEN", 401),
    
    // Resource errors
    NOT_FOUND("NOT_FOUND", 404),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", 409),
    
    // Validation errors
    VALIDATION_ERROR("VALIDATION_ERROR", 422),
    BAD_REQUEST("BAD_REQUEST", 400),
    
    // Rate limiting
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", 429),
    
    // Server errors
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", 503),
    
    // Generic
    UNKNOWN_ERROR("UNKNOWN_ERROR", 500);

    private final String code;
    private final int statusCode;

    ApiErrorCode(String code, int statusCode) {
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
