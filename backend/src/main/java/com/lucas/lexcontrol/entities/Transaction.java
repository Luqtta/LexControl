package com.lucas.lexcontrol.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    public Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(length = 1000)
    public String description;

    @Column(nullable = false)
    public LocalDate date;
}
