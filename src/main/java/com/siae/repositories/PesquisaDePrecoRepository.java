package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.PesquisaDePreco;
import com.siae.entities.Produto;

public interface PesquisaDePrecoRepository extends JpaRepository<PesquisaDePreco, Long> {
	
	PesquisaDePreco findByProduto(Produto produto);
}
