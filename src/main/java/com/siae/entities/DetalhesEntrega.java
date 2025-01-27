package com.siae.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalhesEntrega")
@Getter
@Setter
@NoArgsConstructor
public class DetalhesEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "entrega_id")
    private Entrega entrega;

    private BigDecimal total;
    private BigDecimal quantidade;
}
