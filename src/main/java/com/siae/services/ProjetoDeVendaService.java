package com.siae.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.dto.ProjetoDeVendaDTO;
import com.siae.entities.PesquisaDePreco;
import com.siae.entities.Produto;
import com.siae.entities.Produtor;
import com.siae.entities.ProjetoDeVenda;
import com.siae.entities.ProjetoProduto;
import com.siae.repositories.PesquisaDePrecoRepository;
import com.siae.repositories.ProdutoRepository;
import com.siae.repositories.ProdutorRepository;
import com.siae.repositories.ProjetoDeVendaRepository;
import com.siae.repositories.ProjetoProdutoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProjetoDeVendaService {

	@Autowired
	private ProjetoDeVendaRepository  repository;
	
	@Autowired
	private ProdutorRepository produtorRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ProjetoProdutoRepository projetoProdutoRepository;
	
	@Autowired
	private PesquisaDePrecoRepository pesquisaRepository;
	
	public List<ProjetoDeVenda> findAll() {
		return repository.findAll();
	}
	
	public ProjetoDeVenda insert(ProjetoDeVendaDTO projetoDTO) {
		Produtor produtor = produtorRepository.findById(projetoDTO.getProdutorId())
				.orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
		ProjetoDeVenda projeto = new ProjetoDeVenda();
		projeto.setProdutor(produtor);
		projeto.setDataProjeto(projetoDTO.getDataProjeto());
		repository.save(projeto);
		
		if (projetoDTO.getPesquisasId().size() != projetoDTO.getQuantidade().size()) {
	        throw new IllegalArgumentException("A lista de produtos e a lista de quantidades devem ter o mesmo tamanho.");
	    }
		
		List<ProjetoProduto> projetoProdutos = new ArrayList<>();
		
		for (int i = 0; i < projetoDTO.getPesquisasId().size(); i++) {
			Long pesquisaId = projetoDTO.getPesquisasId().get(i);
			Integer quantidade = projetoDTO.getQuantidade().get(i);
			PesquisaDePreco pesquisa = pesquisaRepository.findById(pesquisaId)
					.orElseThrow(() -> new EntityNotFoundException("Pesquisa não encontrada"));
			pesquisa.setQuantidade(pesquisa.getQuantidade() - quantidade);
			
	        Long produtoId = pesquisa.getProduto().getId();
	        Produto produto = produtoRepository.findById(produtoId)
	                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	        
	        BigDecimal total = produto.getPrecoMedio().multiply(BigDecimal.valueOf(quantidade));
	        ProjetoProduto projetoProduto = new ProjetoProduto(produto, projeto, quantidade, total);
	        
	        projetoProdutos.add(projetoProduto);
	    }
		
		BigDecimal totalGeral = projeto.total(projetoProdutos);
		projeto.setTotal(totalGeral);
		
		projetoProdutoRepository.saveAll(projetoProdutos);
		projeto.setProjetoProdutos(projetoProdutos);
		return projeto;
	}
}
