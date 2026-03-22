package com.lucas.lexcontrol.dto.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String description,
        BigDecimal totalHonorarios,
        BigDecimal valorRecebido,
        BigDecimal valorPendente,
        BigDecimal valorPrevistoSentenca,
        BigDecimal valorPagoSentenca,
        BigDecimal valorPendenteSentenca,
        LocalDateTime createdAt
) {
}
