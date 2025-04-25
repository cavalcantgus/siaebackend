package com.siae.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class PesquisaDePrecoDTO {
	
	private Long produtoId;
	private LocalDate dataPesquisa;
	private BigDecimal quantidade;
	
	@JsonProperty("precos")
	@NotEmpty(message = "Lista de preços não pode ser nula")
	@Valid
	private List<@NotNull(message = "Valor não pode ser nulo") BigDecimal> precos;
}
