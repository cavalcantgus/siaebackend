package com.siae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "entrega")
@Getter
@Setter
@NoArgsConstructor
public class Entrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produtor_id")
    private Produtor produtor;

    @Temporal(TemporalType.DATE)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataDaEntrega;

    @OneToMany(mappedBy = "entrega")
    @JsonManagedReference
    private List<DetalhesEntrega> detalhesEntrega;

    private BigDecimal total;

    private BigDecimal quantidade;

    public BigDecimal valorTotal(List<DetalhesEntrega> detalhesEntrega) {
        return detalhesEntrega.stream().map(DetalhesEntrega::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal quantidadeTotal(List<DetalhesEntrega> detalhesEntrega) {
        return detalhesEntrega.stream().map(DetalhesEntrega::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
