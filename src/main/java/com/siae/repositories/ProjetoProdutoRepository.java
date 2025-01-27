package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.ProjetoProduto;

public interface ProjetoProdutoRepository extends JpaRepository<ProjetoProduto, Long>{
    ProjetoProduto findByProjetoId(Long id);
}
