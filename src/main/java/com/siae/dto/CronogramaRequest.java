package com.siae.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CronogramaRequest {
    private LocalDate dataEntrega;
    private BigDecimal quantidade;
}
