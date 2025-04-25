package com.siae.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projetoProduto")
@Getter
@Setter
@NoArgsConstructor
public class ProjetoProduto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;
	
	@ManyToOne
	@JoinColumn(name = "projeto_id", nullable = false)
	@JsonBackReference
	private ProjetoDeVenda projeto;

	@Temporal(TemporalType.DATE)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate inicioEntrega;

	@Temporal(TemporalType.DATE)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate fimEntrega;
	
	private BigDecimal quantidade;
	private BigDecimal total;
	
	public ProjetoProduto(Produto produto, ProjetoDeVenda projeto, BigDecimal quantidade, BigDecimal total, LocalDate inicioEntrega, LocalDate fimEntrega) {
		this.produto = produto;
		this.projeto = projeto;
		this.quantidade = quantidade;
		this.total = total;
		this.inicioEntrega = inicioEntrega;
		this.fimEntrega = fimEntrega;
	}

	public BigDecimal calculateTotal(BigDecimal precoMedio, BigDecimal quantidade) {
		if(precoMedio == null || quantidade == null) return BigDecimal.ZERO;
		return precoMedio.multiply(quantidade);
	}

}
