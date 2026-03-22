package com.lucas.lexcontrol.dto.client;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;

public class ClientRequest {

    @NotBlank
    @Size(max = 160)
    public String name;

    @Size(max = 1000)
    public String description;

    @NotNull
    @PositiveOrZero
    public BigDecimal totalHonorarios;

    @PositiveOrZero
    public BigDecimal valorRecebido;

    @PositiveOrZero
    public BigDecimal valorPrevistoSentenca;

    @PositiveOrZero
    public BigDecimal valorPagoSentenca;
}
