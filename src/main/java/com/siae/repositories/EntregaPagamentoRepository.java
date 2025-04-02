package com.siae.repositories;

import com.siae.entities.EntregaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntregaPagamentoRepository extends JpaRepository<EntregaPagamento, Long> {
    List<EntregaPagamento> findByPagamentoId(Long id);
    Optional<EntregaPagamento> findByEntregaId(Long entregaId);
}
