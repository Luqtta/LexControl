package com.lucas.lexcontrol.common;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputSanitizer {

    public String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
    }
}
