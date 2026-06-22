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
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public Optional<Transaction> findByIdAndUser(UUID id, UUID userId) {
        return find("id = :id and user.id = :userId",
                Map.of("id", id, "userId", userId)).firstResultOptional();
    }

    public List<Transaction> listByUser(UUID userId, TransactionType type, UUID clientId,
            LocalDate from, LocalDate to, String sort, int page, int size) {
        // Full HQL with LEFT JOIN FETCH so the to-one client is loaded in the same query,
        // avoiding an N+1 when the response reads client.name. The fetch is on a @ManyToOne,
        // so SQL-level pagination (LIMIT/OFFSET) stays correct.
        StringBuilder query = new StringBuilder(
                "from Transaction t left join fetch t.client where t.user.id = :userId");
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (type != null) {
            query.append(" and t.type = :type");
            params.put("type", type);
        }
        if (clientId != null) {
            query.append(" and t.client.id = :clientId");
            params.put("clientId", clientId);
        }
        if (from != null) {
            query.append(" and t.date >= :from");
            params.put("from", from);
        }
        if (to != null) {
            query.append(" and t.date <= :to");
            params.put("to", to);
        }

        query.append(" order by ").append(parseOrderBy(sort));

        return find(query.toString(), params)
                .page(Page.of(page, size))
                .list();
    }

    private String parseOrderBy(String sort) {
        if ("amount".equalsIgnoreCase(sort)) {
            return "t.amount desc";
        }
        return "t.date desc";
    }
}
