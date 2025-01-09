package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long>{

}
