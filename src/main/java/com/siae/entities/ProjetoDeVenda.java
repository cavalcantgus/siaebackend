package com.siae.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projetoDeVenda")
@Getter
@Setter
@NoArgsConstructor
public class ProjetoDeVenda {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Temporal(TemporalType.DATE)
	@JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dataProjeto;
	
	@ManyToOne
    @JoinColumn(name = "produtor_id", nullable = false)
    private Produtor produtor;
	
	@OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<ProjetoProduto> projetoProdutos;
	
	private BigDecimal total;
	
	public BigDecimal total(List<ProjetoProduto> projetoProdutos) {
		return projetoProdutos.stream().map(ProjetoProduto::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
}
