package com.lucas.lexcontrol.common;

public record ValidationError(
        String field,
        String message
) {
}
