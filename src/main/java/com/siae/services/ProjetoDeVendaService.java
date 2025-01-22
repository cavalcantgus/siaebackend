package com.siae.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	
	public ProjetoDeVenda findById(Long id) {
		Optional<ProjetoDeVenda> projeto = repository.findById(id);
		return projeto.orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));
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
			BigDecimal quantidade = projetoDTO.getQuantidade().get(i);
			PesquisaDePreco pesquisa = pesquisaRepository.findById(pesquisaId)
					.orElseThrow(() -> new EntityNotFoundException("Pesquisa não encontrada"));
			pesquisa.setQuantidade(pesquisa.getQuantidade().subtract(quantidade));
			
	        Long produtoId = pesquisa.getProduto().getId();
	        Produto produto = produtoRepository.findById(produtoId)
	                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	        
	        BigDecimal total = produto.getPrecoMedio().multiply(quantidade);
	        ProjetoProduto projetoProduto = new ProjetoProduto(produto, projeto, quantidade, total);
	        
	        projetoProdutos.add(projetoProduto);
	    }
		
		BigDecimal totalGeral = projeto.total(projetoProdutos);
		BigDecimal quantidadeTotal = projeto.quantidadeTotal(projetoProdutos);
		projeto.setTotal(totalGeral);
		projeto.setQuantidadeTotal(quantidadeTotal);
		
		projetoProdutoRepository.saveAll(projetoProdutos);
		projeto.setProjetoProdutos(projetoProdutos);
		return projeto;
	}
	
	public ProjetoDeVenda update(Long id, ProjetoDeVenda projeto) {
		try {
			ProjetoDeVenda projetoDeVenda = repository.findById(projeto.getId())
					.orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));
			updateData(projeto, projetoDeVenda);
			return repository.save(projetoDeVenda);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
			
	}

	private void updateData(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda) {
	    Produtor produtor = produtorRepository.findById(projeto.getProdutor().getId())
	            .orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
	    projetoDeVenda.setProdutor(produtor);
	    projetoDeVenda.setDataProjeto(projeto.getDataProjeto());

	    List<ProjetoProduto> projetoProdutosExistentes = projetoDeVenda.getProjetoProdutos();
	    projetoProdutosExistentes.clear();

	    projeto.getProjetoProdutos().forEach(projetoProduto -> {
	        Produto produto = produtoRepository.findById(projetoProduto.getProduto().getId())
	                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

	        PesquisaDePreco pesquisa = pesquisaRepository.findByProdutoId(produto.getId());

	        if (projetoProduto.getId() == null) {
	            // Criar um novo ProjetoProduto
	            BigDecimal quantidade = projetoProduto.getQuantidade();
	            BigDecimal total = produto.getPrecoMedio().multiply(quantidade);

	            ProjetoProduto novoProjetoProduto = new ProjetoProduto();
	            novoProjetoProduto.setProduto(produto);
	            novoProjetoProduto.setQuantidade(quantidade);
	            novoProjetoProduto.setTotal(total);
	            novoProjetoProduto.setProjeto(projetoDeVenda);

	            projetoProdutosExistentes.add(novoProjetoProduto);
	        } else {
	            // Atualizar um ProjetoProduto existente
	            ProjetoProduto projetoAnterior = projetoProdutoRepository.findById(projetoProduto.getId())
	                    .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));

	            BigDecimal quantidadeAnterior = projetoAnterior.getQuantidade();
	            BigDecimal quantidade = projetoProduto.getQuantidade();

	            pesquisa.setQuantidade(pesquisa.getQuantidade().add(quantidadeAnterior).subtract(quantidade));
	            pesquisaRepository.save(pesquisa);

	            BigDecimal total = produto.getPrecoMedio().multiply(quantidade);
	            projetoAnterior.setProduto(produto);
	            projetoAnterior.setQuantidade(quantidade);
	            projetoAnterior.setTotal(total);
	            projetoAnterior.setProjeto(projetoDeVenda);

	            projetoProdutosExistentes.add(projetoAnterior);
	        }
	    });

	    projetoProdutoRepository.saveAll(projetoProdutosExistentes);
	    projetoDeVenda.setProjetoProdutos(projetoProdutosExistentes);

	    BigDecimal quantidadeTotal = projetoDeVenda.quantidadeTotal(projetoProdutosExistentes);
	    BigDecimal total = projetoDeVenda.total(projetoProdutosExistentes);
	    projetoDeVenda.setQuantidadeTotal(quantidadeTotal);
	    projetoDeVenda.setTotal(total);
	}

}
