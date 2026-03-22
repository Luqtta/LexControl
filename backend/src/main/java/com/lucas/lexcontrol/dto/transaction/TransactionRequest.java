package com.lucas.lexcontrol.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.lucas.lexcontrol.entities.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TransactionRequest {

    @NotNull
    public TransactionType type;

    @NotNull
    @Positive
    public BigDecimal amount;

    @NotNull
    public LocalDate date;

    @Size(max = 1000)
    public String description;

    public UUID clientId;
}
