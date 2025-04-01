package com.siae.repositories;

import com.siae.entities.Pagamento;
import com.siae.entities.Produtor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    @Query("SELECT p FROM Pagamento p WHERE p.produtor = :produtor AND YEAR(p.data) = :ano AND MONTH(p.data) = :mes")
    Pagamento findByProdutorAndAnoAndMes(@Param("produtor") Produtor produtor,
                                               @Param("ano") int ano,
                                               @Param("mes") int mes);
}
