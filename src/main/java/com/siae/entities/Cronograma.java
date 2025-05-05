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
@Getter
@Setter
@NoArgsConstructor
public class Cronograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal total;
    private  BigDecimal quantidade;

    @ManyToOne
    private Produtor produtor;

    @OneToMany(mappedBy = "cronograma", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalhesCronograma> detalhesCronograma;

    public BigDecimal valorTotal(List<DetalhesCronograma> detalhesCronograma) {
        return detalhesCronograma.stream().map(DetalhesCronograma::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal quantidadeTotal(List<DetalhesCronograma> detalhesCronograma) {
        return detalhesCronograma.stream().map(DetalhesCronograma::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
