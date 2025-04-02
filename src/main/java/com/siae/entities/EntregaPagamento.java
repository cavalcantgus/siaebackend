package com.siae.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entrega_pagamento")
@NoArgsConstructor
@Getter
@Setter
public class EntregaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "entrega_id", unique = true)
    private Entrega entrega;

    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;
}
