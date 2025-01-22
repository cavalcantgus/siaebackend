package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.PesquisaDePreco;

public interface PesquisaDePrecoRepository extends JpaRepository<PesquisaDePreco, Long> {
	
	PesquisaDePreco findByProdutoId(Long produtoId);
}
