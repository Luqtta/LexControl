package com.lucas.lexcontrol.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummary(
        BigDecimal totalHonorarios,
        BigDecimal totalRecebido,
        BigDecimal totalPendente,
        BigDecimal recebidoMes,
        BigDecimal gastosMes,
        BigDecimal saldoAtual,
        BigDecimal totalGastos,
        BigDecimal totalCreditos,
        BigDecimal sentencaPrevista,
        BigDecimal sentencaPaga,
        BigDecimal sentencaPendente
) {
}
