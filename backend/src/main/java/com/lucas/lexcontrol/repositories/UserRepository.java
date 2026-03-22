package com.lucas.lexcontrol.repositories;

import java.util.Optional;
import java.util.UUID;

import com.lucas.lexcontrol.entities.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return find("lower(email)", email.toLowerCase()).firstResultOptional();
    }

    public Optional<User> findByIdOptional(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return find("id", id).firstResultOptional();
    }
}
