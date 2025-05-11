package com.siae.repositories;

import com.siae.entities.Cronograma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {
    List<Cronograma> findByProdutorId(Long produtorId);
}
