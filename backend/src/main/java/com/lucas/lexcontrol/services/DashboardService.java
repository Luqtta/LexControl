package com.lucas.lexcontrol.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.lucas.lexcontrol.dto.dashboard.DashboardSummary;
import com.lucas.lexcontrol.entities.TransactionType;
import com.lucas.lexcontrol.security.CurrentUser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class DashboardService {

    @Inject
    CurrentUser currentUser;

    @Inject
    EntityManager entityManager;

    public DashboardSummary summary() {
        UUID userId = currentUser.getUserId();

        BigDecimal totalHonorarios = sumClientField(userId, "totalHonorarios");
        BigDecimal totalRecebido = sumClientField(userId, "valorRecebido");
        BigDecimal totalPendente = totalHonorarios.subtract(totalRecebido);
        if (totalPendente.compareTo(BigDecimal.ZERO) < 0) {
            totalPendente = BigDecimal.ZERO;
        }

        BigDecimal sentencaPrevista = sumClientField(userId, "valorPrevistoSentenca");
        BigDecimal sentencaPaga = sumClientField(userId, "valorPagoSentenca");
        BigDecimal sentencaPendente = sentencaPrevista.subtract(sentencaPaga);
        if (sentencaPendente.compareTo(BigDecimal.ZERO) < 0) {
            sentencaPendente = BigDecimal.ZERO;
        }

        BigDecimal totalGastos = sumTransactions(userId, TransactionType.EXPENSE, null, null);
        BigDecimal totalCreditos = sumTransactions(userId, TransactionType.INCOME, null, null);
        BigDecimal saldoAtual = totalCreditos.subtract(totalGastos);

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        BigDecimal recebidoMes = sumTransactions(userId, TransactionType.INCOME, start, end);
        BigDecimal gastosMes = sumTransactions(userId, TransactionType.EXPENSE, start, end);

        return new DashboardSummary(
                totalHonorarios,
                totalRecebido,
                totalPendente,
                recebidoMes,
                gastosMes,
                saldoAtual,
                totalGastos,
                totalCreditos,
                sentencaPrevista,
                sentencaPaga,
                sentencaPendente
        );
    }

    private BigDecimal sumClientField(UUID userId, String field) {
        String jpql = "select coalesce(sum(c." + field + "), 0) from Client c where c.user.id = :userId";
        return (BigDecimal) entityManager.createQuery(jpql)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    private BigDecimal sumTransactions(UUID userId, TransactionType type, LocalDate from, LocalDate to) {
        StringBuilder jpql = new StringBuilder("select coalesce(sum(t.amount), 0) from Transaction t where t.user.id = :userId and t.type = :type");
        if (from != null) {
            jpql.append(" and t.date >= :from");
        }
        if (to != null) {
            jpql.append(" and t.date <= :to");
        }
        var query = entityManager.createQuery(jpql.toString())
                .setParameter("userId", userId)
                .setParameter("type", type);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        return (BigDecimal) query.getSingleResult();
    }
}
