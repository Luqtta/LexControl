package com.lucas.lexcontrol.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.lucas.lexcontrol.entities.TransactionType;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate date,
        UUID clientId,
        String clientName,
        LocalDateTime createdAt
) {
}
