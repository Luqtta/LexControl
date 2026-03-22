package com.lucas.lexcontrol.common;

import java.util.List;

public record ApiError(
        String timestamp,
        int status,
        String code,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors
) {
}
