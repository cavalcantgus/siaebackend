package com.siae.services;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class ProjetoDeVendaService {

	@Autowired
	private ProjetoDeVendaRepository repository;

	@Autowired
	private ProdutorRepository produtorRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private ProjetoProdutoRepository projetoProdutoRepository;

	@Autowired
	private PesquisaDePrecoRepository pesquisaRepository;

	@PersistenceContext
	private EntityManager entityManager;

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
			throw new IllegalArgumentException(
					"A lista de produtos e a lista de quantidades devem ter o mesmo tamanho.");
		}

		List<ProjetoProduto> projetoProdutos = new ArrayList<>();

		for (int i = 0; i < projetoDTO.getPesquisasId().size(); i++) {
			LocalDate inicioEntrega = projetoDTO.getInicioEntrega().get(i);
			LocalDate fimEntrega = projetoDTO.getFimEntrega().get(i);
			Long pesquisaId = projetoDTO.getPesquisasId().get(i);
			BigDecimal quantidade = projetoDTO.getQuantidade().get(i);
			PesquisaDePreco pesquisa = pesquisaRepository.findById(pesquisaId)
					.orElseThrow(() -> new EntityNotFoundException("Pesquisa não encontrada"));
			pesquisa.setQuantidade(pesquisa.getQuantidade().subtract(quantidade));

			Long produtoId = pesquisa.getProduto().getId();
			Produto produto = produtoRepository.findById(produtoId)
					.orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

			BigDecimal total = produto.getPrecoMedio().multiply(quantidade);
			ProjetoProduto projetoProduto = new ProjetoProduto(produto, projeto, quantidade, total, inicioEntrega, fimEntrega);

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

	@Transactional
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

	@Transactional
	protected void updateData(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda) {
	    System.out.println("Iniciando o método updateData.");

	    Produtor produtor = produtorRepository.findById(projeto.getProdutor().getId())
	            .orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
	    projetoDeVenda.setProdutor(produtor);
	    projetoDeVenda.setDataProjeto(projeto.getDataProjeto());

	    List<ProjetoProduto> projetoProdutosExistentes = projetoDeVenda.getProjetoProdutos(); // Projeto 40, Produto 1, BOLO

	    if (projeto.getProjetoProdutos().size() == 1 && projetoProdutosExistentes.size() == 1) {
	        handleSingleProjetoProduto(projeto, projetoDeVenda, projetoProdutosExistentes.get(0));
	    } else {
	        handleMultipleProjetoProdutos(projeto, projetoDeVenda, projetoProdutosExistentes);
	    }

	    // Calcular total e quantidade
	    BigDecimal quantidadeTotal = projetoDeVenda.quantidadeTotal(projetoDeVenda.getProjetoProdutos());
	    BigDecimal total = projetoDeVenda.total(projetoDeVenda.getProjetoProdutos());
	    projetoDeVenda.setQuantidadeTotal(quantidadeTotal);
	    projetoDeVenda.setTotal(total);

	    // Salvar as atualizações no banco
	    for(ProjetoProduto p : projetoProdutosExistentes) {
	    	System.out.println("Projeto: " + p.getId());
	    }
	    projetoProdutoRepository.saveAll(projetoDeVenda.getProjetoProdutos());
	    projetoDeVenda.setProjetoProdutos(projetoDeVenda.getProjetoProdutos());
	}

	private void handleSingleProjetoProduto(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda, ProjetoProduto projetoProdutoExistente) {
	    projeto.getProjetoProdutos().forEach(projetoProduto -> {
	        if (!projetoProdutoExistente.getProduto().getId().equals(projetoProduto.getProduto().getId())) {
	            System.out.println("Produto alterado detectado. Atualizando produto.");
	            
	            // Repor quantidade ao estoque do produto antigo
	            devolverQuantidadeAoEstoque(projetoProdutoExistente);

	            // Remover o produto antigo
	            projetoProdutoRepository.delete(projetoProdutoExistente);

	            // Adicionar o novo produto
	            adicionarNovoProjetoProduto(projetoProduto, projetoDeVenda);
	            projetoDeVenda.getProjetoProdutos().remove(0);
	        } else {
	            atualizarProjetoProdutoExistente(projetoProdutoExistente, projetoProduto);
	        }
	    });
	}

	private void handleMultipleProjetoProdutos(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda, List<ProjetoProduto> projetoProdutosExistentes) {
	    // Remover produtos que não estão mais no payload
	    projetoProdutosExistentes.removeIf(projetoProdAnt -> {
	        boolean shouldRemove = projeto.getProjetoProdutos().stream()
	                .noneMatch(projetoProd -> projetoProd.getId().equals(projetoProdAnt.getId()));

	        if (shouldRemove) {
	            devolverQuantidadeAoEstoque(projetoProdAnt);
	            projetoProdutoRepository.delete(projetoProdAnt);
	        }

	        return shouldRemove;
	    });

	    // Atualizar ou adicionar novos produtos
	    projeto.getProjetoProdutos().forEach(projetoProduto -> {
	        if (projetoProduto.getId() == null) {
	            adicionarNovoProjetoProduto(projetoProduto, projetoDeVenda);
	        } else {
	            ProjetoProduto projetoProdutoExistente = projetoProdutoRepository.findById(projetoProduto.getId())
	                    .orElseThrow(() -> new EntityNotFoundException("ProjetoProduto não encontrado"));
	            atualizarProjetoProdutoExistente(projetoProdutoExistente, projetoProduto);
	        }
	    });
	}

	private void devolverQuantidadeAoEstoque(ProjetoProduto projetoProduto) {
	    Produto produto = produtoRepository.findById(projetoProduto.getProduto().getId())
	            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	    PesquisaDePreco pesquisa = pesquisaRepository.findByProdutoId(produto.getId());

	    pesquisa.setQuantidade(pesquisa.getQuantidade().add(projetoProduto.getQuantidade()));
	    pesquisaRepository.save(pesquisa);
	    System.out.println("Quantidade devolvida ao estoque do produto " + produto.getId());
	}

	private void adicionarNovoProjetoProduto(ProjetoProduto projetoProduto, ProjetoDeVenda projetoDeVenda) {
	    Produto produto = produtoRepository.findById(projetoProduto.getProduto().getId())
	            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	    PesquisaDePreco pesquisa = pesquisaRepository.findByProdutoId(produto.getId());

	    pesquisa.setQuantidade(pesquisa.getQuantidade().subtract(projetoProduto.getQuantidade()));
	    pesquisaRepository.save(pesquisa);

	    BigDecimal total = produto.getPrecoMedio().multiply(projetoProduto.getQuantidade());

	    ProjetoProduto novoProjetoProduto = new ProjetoProduto();
	    novoProjetoProduto.setProduto(produto);
	    novoProjetoProduto.setQuantidade(projetoProduto.getQuantidade());
	    novoProjetoProduto.setTotal(total);
		novoProjetoProduto.setInicioEntrega(projetoProduto.getInicioEntrega());
		novoProjetoProduto.setFimEntrega(projetoProduto.getFimEntrega());
		novoProjetoProduto.setProjeto(projetoDeVenda);

	    projetoDeVenda.getProjetoProdutos().add(novoProjetoProduto);
	    for(ProjetoProduto p : projetoDeVenda.getProjetoProdutos()) {
	    	System.out.println("Projeto: " + p.getId());
	    }
	    System.out.println("Novo produto adicionado: " + novoProjetoProduto.getId());
	}

	private void atualizarProjetoProdutoExistente(ProjetoProduto projetoProdutoExistente, ProjetoProduto projetoProdutoAtualizado) {
	    BigDecimal quantidadeAnterior = projetoProdutoExistente.getQuantidade();
	    BigDecimal novaQuantidade = projetoProdutoAtualizado.getQuantidade();

	    Produto produto = produtoRepository.findById(projetoProdutoExistente.getProduto().getId())
	            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	    PesquisaDePreco pesquisa = pesquisaRepository.findByProdutoId(produto.getId());

	    pesquisa.setQuantidade(pesquisa.getQuantidade().add(quantidadeAnterior).subtract(novaQuantidade));
	    pesquisaRepository.save(pesquisa);

	    BigDecimal total = produto.getPrecoMedio().multiply(novaQuantidade);
		projetoProdutoExistente.setInicioEntrega(projetoProdutoAtualizado.getInicioEntrega());
		projetoProdutoExistente.setFimEntrega(projetoProdutoAtualizado.getFimEntrega());
	    projetoProdutoExistente.setQuantidade(novaQuantidade);
	    projetoProdutoExistente.setTotal(total);
	    System.out.println("ProjetoProduto atualizado: " + projetoProdutoExistente.getId());
	}


	public void deleteById(Long id) {
		try {
			if (id != null && repository.existsById(id)) {
				ProjetoDeVenda projetoDeVenda = repository.findById(id)
						.orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));

				projetoDeVenda.getProjetoProdutos().forEach(projetoProduto -> {
					Long produtoId = projetoProduto.getProduto().getId();
					PesquisaDePreco pesquisa = pesquisaRepository.findByProdutoId(produtoId);
					pesquisa.setQuantidade(pesquisa.getQuantidade().add(projetoProduto.getQuantidade()));
					pesquisaRepository.save(pesquisa);
				});
				repository.deleteById(id);
			} else {
				throw new EntityNotFoundException("Projeto não encontrado");
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}
}
