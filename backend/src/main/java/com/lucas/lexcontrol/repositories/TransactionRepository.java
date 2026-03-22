package com.lucas.lexcontrol.repositories;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.lucas.lexcontrol.entities.Transaction;
import com.lucas.lexcontrol.entities.TransactionType;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public Optional<Transaction> findByIdAndUser(UUID id, UUID userId) {
        return find("id = :id and user.id = :userId",
                Map.of("id", id, "userId", userId)).firstResultOptional();
    }

    public List<Transaction> listByUser(UUID userId, TransactionType type, UUID clientId,
            LocalDate from, LocalDate to, String sort) {
        StringBuilder query = new StringBuilder("user.id = :userId");
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (type != null) {
            query.append(" and type = :type");
            params.put("type", type);
        }
        if (clientId != null) {
            query.append(" and client.id = :clientId");
            params.put("clientId", clientId);
        }
        if (from != null) {
            query.append(" and date >= :from");
            params.put("from", from);
        }
        if (to != null) {
            query.append(" and date <= :to");
            params.put("to", to);
        }

        Sort sortClause = parseSort(sort);
        return find(query.toString(), sortClause, params).list();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("date").descending();
        }
        if ("amount".equalsIgnoreCase(sort)) {
            return Sort.by("amount").descending();
        }
        if ("date".equalsIgnoreCase(sort)) {
            return Sort.by("date").descending();
        }
        return Sort.by("date").descending();
    }
}
