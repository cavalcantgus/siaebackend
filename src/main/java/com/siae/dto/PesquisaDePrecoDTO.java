package com.siae.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	private List<BigDecimal> preços;
}
