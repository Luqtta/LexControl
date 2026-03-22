package com.lucas.lexcontrol.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User extends BaseEntity {

    @Column(nullable = false, length = 120)
    public String name;

    @Column(nullable = false, length = 160)
    public String email;

    @Column(name = "password_hash", nullable = false, length = 120)
    public String passwordHash;
}
