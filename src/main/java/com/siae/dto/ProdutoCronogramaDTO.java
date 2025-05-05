package com.siae.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProdutoCronogramaDTO {
    private Long produtoId;
    private List<CronogramaRequest> cronogramas;
}
