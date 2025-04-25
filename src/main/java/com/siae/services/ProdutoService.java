package com.siae.services;

import java.util.List;
import java.util.Optional;

import com.siae.exception.ResourceNotFoundException;
import com.siae.mappers.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.entities.Produto;
import com.siae.repositories.ProdutoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoService {
	
	private final ProdutoRepository repository;
    private final ProductMapper productMapper;

	@Autowired
	public ProdutoService(ProdutoRepository repository, ProductMapper productMapper) {
		this.repository = repository;
		this.productMapper = productMapper;
	}

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
		Produto produtoTarget =
				repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Produto"
						, id));
		productMapper.updateProduct(produtoTarget, produto);
		return repository.save(produtoTarget);
	}
	
	public void deleteById(Long id) {
		Produto produto =  repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Produto", id));
		repository.delete(produto);
	}
}
