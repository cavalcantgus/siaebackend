package com.siae.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.Documento;
import com.siae.entities.Produtor;

public interface DocumentoRepository extends JpaRepository<Documento, Long>{
	List<Documento> findByProdutor(Produtor produtor);
}
