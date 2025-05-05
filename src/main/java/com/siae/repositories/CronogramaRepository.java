package com.siae.repositories;

import com.siae.entities.Cronograma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {
}
