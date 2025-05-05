package com.siae.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CronogramaDTO {

    private Long produtorId;
    List<ProdutoCronogramaDTO> detalhes;
}
