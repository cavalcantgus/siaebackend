package com.siae.dto;

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
	private List<Long> pesquisasId;
	private List<Integer> quantidade;
}
