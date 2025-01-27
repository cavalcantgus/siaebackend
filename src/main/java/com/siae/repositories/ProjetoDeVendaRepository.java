package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.ProjetoDeVenda;

public interface ProjetoDeVendaRepository extends JpaRepository<ProjetoDeVenda, Long>{
    ProjetoDeVenda findByProdutorId(Long id);
}
