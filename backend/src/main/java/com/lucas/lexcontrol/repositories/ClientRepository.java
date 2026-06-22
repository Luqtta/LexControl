package com.lucas.lexcontrol.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.lucas.lexcontrol.entities.Client;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientRepository implements PanacheRepository<Client> {

    public Optional<Client> findByIdAndUser(UUID id, UUID userId) {
        return find("id = :id and user.id = :userId",
                Map.of("id", id, "userId", userId)).firstResultOptional();
    }

    public List<Client> listByUser(UUID userId, String search, String status, String sort,
            int page, int size) {
        StringBuilder query = new StringBuilder("user.id = :userId");
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (search != null && !search.isBlank()) {
            query.append(" and lower(name) like :search");
            params.put("search", "%" + search.toLowerCase() + "%");
        }

        if (status != null && !status.isBlank()) {
            if ("PENDING".equalsIgnoreCase(status)) {
                query.append(" and totalHonorarios > valorRecebido");
            } else if ("PAID".equalsIgnoreCase(status)) {
                query.append(" and totalHonorarios <= valorRecebido");
            }
        }

        Sort sortClause = parseSort(sort);
        return find(query.toString(), sortClause, params)
                .page(Page.of(page, size))
                .list();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("createdAt").descending();
        }
        if ("name".equalsIgnoreCase(sort)) {
            return Sort.by("name").ascending();
        }
        if ("totalHonorarios".equalsIgnoreCase(sort)) {
            return Sort.by("totalHonorarios").descending();
        }
        if ("valorRecebido".equalsIgnoreCase(sort)) {
            return Sort.by("valorRecebido").descending();
        }
        if ("createdAt".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").descending();
        }
        return Sort.by("createdAt").descending();
    }
}
