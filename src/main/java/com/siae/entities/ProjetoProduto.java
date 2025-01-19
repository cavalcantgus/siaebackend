package com.siae.entities;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
	
	private Integer quantidade;
	private BigDecimal total;
	
	public ProjetoProduto(Produto produto, ProjetoDeVenda projeto, Integer quantidade, BigDecimal total) {
		this.produto = produto;
		this.projeto = projeto;
		this.quantidade = quantidade;
		this.total = total;
		
	}
}
