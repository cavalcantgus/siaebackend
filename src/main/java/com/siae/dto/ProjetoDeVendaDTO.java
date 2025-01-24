package com.siae.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjetoDeVendaDTO {
	
	private Long produtorId;
	private LocalDate dataProjeto;
	private List<LocalDate> inicioEntrega;
	private List<LocalDate> fimEntrega;
	private List<Long> pesquisasId;
	private List<BigDecimal> quantidade;
}
