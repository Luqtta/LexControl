package com.lucas.lexcontrol.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clients")
public class Client extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(nullable = false, length = 160)
    public String name;

    @Column(length = 1000)
    public String description;

    @Column(name = "total_honorarios", nullable = false, precision = 19, scale = 2)
    public BigDecimal totalHonorarios;

    @Column(name = "valor_recebido", nullable = false, precision = 19, scale = 2)
    public BigDecimal valorRecebido;

    @Column(name = "valor_previsto_sentenca", nullable = false, precision = 19, scale = 2)
    public BigDecimal valorPrevistoSentenca;

    @Column(name = "valor_pago_sentenca", nullable = false, precision = 19, scale = 2)
    public BigDecimal valorPagoSentenca;
}
