package com.siae.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pesquisaDePreco")
@Getter
@Setter
@NoArgsConstructor
public class PesquisaDePreco {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dataPesquisa;

	@OneToOne
	@JoinColumn(name = "produto_id", unique = true, nullable = false)
	@JsonManagedReference
	private Produto produto;
	
	private BigDecimal precoMedio;

    @OneToMany(mappedBy = "pesquisa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<Preco> precos = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public BigDecimal precoMedio() {
	    // Filtrar valores nulos antes de calcular
	    List<BigDecimal> valores = precos.stream()
	        .map(Preco::getValor)
	        .filter(Objects::nonNull) // Ignorar valores nulos
	        .collect(Collectors.toList());

	    if (valores.isEmpty()) {
	        return BigDecimal.ZERO; // Evitar divisão por zero
	    }

	    BigDecimal soma = valores.stream()
	        .reduce(BigDecimal.ZERO, BigDecimal::add);

	    return soma.divide(BigDecimal.valueOf(valores.size()), 2);
	}


}
