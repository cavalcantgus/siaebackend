package com.siae.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.entities.Produto;
import com.siae.repositories.ProdutoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoService {
	
	@Autowired
	private ProdutoRepository repository;
	
	public List<Produto> findAll() {
		return repository.findAll();
	}
	
	public Produto findById(Long id) {
		Optional<Produto> produto = repository.findById(id);
		return produto.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	}
	
	public Produto insert(Produto produto) {
		return repository.save(produto);
	}
	
	public Produto update(Long id, Produto produto) {
		try { 
			Produto produtoTarget = repository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
			updateData(produto, produtoTarget);
			return repository.save(produtoTarget);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void updateData(Produto produto, Produto produtoTarget) {
		produtoTarget.setDescricao(produto.getDescricao());
		produtoTarget.setEspecificacao(produto.getEspecificacao());
		produtoTarget.setUnidade(produto.getUnidade());
		produtoTarget.setPrecoMedio(produto.getPrecoMedio());
	}
}
