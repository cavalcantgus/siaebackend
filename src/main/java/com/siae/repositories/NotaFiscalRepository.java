package com.siae.repositories;

import com.siae.entities.NotaFiscal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, Long> {
    NotaFiscal findByPagamentoId(Long id);
}
