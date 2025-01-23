package com.siae.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
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

	@Transactional
	protected void updateData(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda) {
		System.out.println("Iniciando o método updateData.");

		Produtor produtor = produtorRepository.findById(projeto.getProdutor().getId())
				.orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
		projetoDeVenda.setProdutor(produtor);
		projetoDeVenda.setDataProjeto(projeto.getDataProjeto());

		List<ProjetoProduto> projetoProdutosExistentes = projetoDeVenda.getProjetoProdutos();

		if (projetoProdutosExistentes.size() == 1) {
			ProjetoProduto p = projetoProdutosExistentes.get(0); // Garantido que só existe um produto

			// Verifica se o produto foi alterado
			projeto.getProjetoProdutos().forEach(projetoProduto -> {
				if (!projetoProdutosExistentes.contains(projetoProduto)) {
					System.out.println("Produto alterado detectado. Removendo e criando novo produto.");

					// Devolver a quantidade à PesquisaDePreco para o produto removido
					Long produtoId = p.getProduto().getId();
					PesquisaDePreco pesquisaAnt = pesquisaRepository.findByProdutoId(produtoId);

					if (pesquisaAnt != null) {
						System.out.println("Quantidade antes de devolver à pesquisaAnt: " + pesquisaAnt.getQuantidade());
						pesquisaAnt.setQuantidade(pesquisaAnt.getQuantidade().add(p.getQuantidade()));
						System.out.println("Quantidade após devolver à pesquisaAnt: " + pesquisaAnt.getQuantidade());
						pesquisaRepository.save(pesquisaAnt);
					}

					// Deletar o ProjetoProduto antigo
					projetoProdutoRepository.delete(p);

					// Adicionar o novo produto
					BigDecimal total = projetoProduto.getQuantidade().multiply(projetoProduto.getProduto().getPrecoMedio());
					Long produtoId1 = projetoProduto.getProduto().getId();
					PesquisaDePreco newPesquisa = pesquisaRepository.findByProdutoId(produtoId1);

					if (newPesquisa != null) {
						System.out.println("Quantidade antes de subtrair da newPesquisa: " + newPesquisa.getQuantidade());
						newPesquisa.setQuantidade(newPesquisa.getQuantidade().subtract(projetoProduto.getQuantidade()));
						System.out.println("Quantidade após subtrair da newPesquisa: " + newPesquisa.getQuantidade());
						pesquisaRepository.save(newPesquisa);
					} else {
						throw new EntityNotFoundException("Pesquisa não encontrada para o novo produto");
					}

					ProjetoProduto newProjetoProduto = new ProjetoProduto();
					newProjetoProduto.setProduto(projetoProduto.getProduto());
					newProjetoProduto.setQuantidade(projetoProduto.getQuantidade());
					newProjetoProduto.setTotal(total);
					newProjetoProduto.setProjeto(projetoDeVenda);
					projetoProdutoRepository.save(newProjetoProduto);

					projetoProdutosExistentes.add(newProjetoProduto); // Adiciona o novo produto à lista
				}
			});
		}

		// Remover produtos que não estão mais no payload e atualizar quantidade na PesquisaDePreco
		projetoProdutosExistentes.removeIf(projetoProdAnt -> {
			boolean shouldRemove = projeto.getProjetoProdutos().stream()
					.noneMatch(projetoProd -> projetoProd.getId().equals(projetoProdAnt.getId()));

			if (shouldRemove) {
				// Antes de deletar, devolver a quantidade à PesquisaDePreco
				Produto produto = projetoProdAnt.getProduto();
				PesquisaDePreco pesquisa = pesquisaRepository.findByProdutoId(produto.getId());

				// Devolver a quantidade ao estoque de PesquisaDePreco
				pesquisa.setQuantidade(pesquisa.getQuantidade().add(projetoProdAnt.getQuantidade()));
				pesquisaRepository.save(pesquisa);

				// Deletar o ProjetoProduto
				projetoProdutoRepository.delete(projetoProdAnt);
			}

			return shouldRemove;
		});

		// Para cada produto no payload, atualizar ou adicionar
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

				// Atualizar a quantidade da PesquisaDePreco
				pesquisa.setQuantidade(pesquisa.getQuantidade().subtract(quantidade));
				pesquisaRepository.save(pesquisa);

				projetoProdutosExistentes.add(novoProjetoProduto);
			} else {
				// Atualizar um ProjetoProduto existente
				ProjetoProduto projetoAnterior = projetoProdutoRepository.findById(projetoProduto.getId())
						.orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));

				BigDecimal quantidadeAnterior = projetoAnterior.getQuantidade();
				BigDecimal quantidade = projetoProduto.getQuantidade();

				// Atualizar a quantidade na pesquisa
				pesquisa.setQuantidade(pesquisa.getQuantidade().add(quantidadeAnterior).subtract(quantidade));
				pesquisaRepository.save(pesquisa);

				BigDecimal total = produto.getPrecoMedio().multiply(quantidade);
				projetoAnterior.setProduto(produto);
				projetoAnterior.setQuantidade(quantidade);
				projetoAnterior.setTotal(total);
				projetoAnterior.setProjeto(projetoDeVenda);

				// Verificar se o projetoProduto já existe na lista antes de adicionar
				if (!projetoProdutosExistentes.contains(projetoAnterior)) {
					projetoProdutosExistentes.add(projetoAnterior);
				}
			}
		});

		// Salvar todos os produtos atualizados ou novos
		projetoProdutoRepository.saveAll(projetoProdutosExistentes);
		projetoDeVenda.setProjetoProdutos(projetoProdutosExistentes);

		// Calcular total e quantidade
		BigDecimal quantidadeTotal = projetoDeVenda.quantidadeTotal(projetoProdutosExistentes);
		BigDecimal total = projetoDeVenda.total(projetoProdutosExistentes);
		projetoDeVenda.setQuantidadeTotal(quantidadeTotal);
		projetoDeVenda.setTotal(total);
	}

	public void deleteById(Long id) {
		try {
			if(id != null && repository.existsById(id)) {
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
