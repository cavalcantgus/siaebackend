package com.siae.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.siae.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.dto.PesquisaDePrecoDTO;
import com.siae.entities.PesquisaDePreco;
import com.siae.entities.Preco;
import com.siae.entities.Produto;
import com.siae.repositories.PesquisaDePrecoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PesquisaDePrecoService {

	private final PesquisaDePrecoRepository repository;
	private final ProdutoService produtoService;

	@Autowired
	public PesquisaDePrecoService(PesquisaDePrecoRepository repository, ProdutoService produtoService) {
		this.repository = repository;
		this.produtoService = produtoService;
	}

	public List<PesquisaDePreco> findAll() {
		return repository.findAll();
	}
	
	public PesquisaDePreco findById(Long id) {
		Optional<PesquisaDePreco> pesquisa = repository.findById(id);
		return pesquisa.orElseThrow(() -> new EntityNotFoundException("Pesquisa não encontrada"));
	}

	public PesquisaDePreco insert(PesquisaDePrecoDTO pesquisa) {
		Produto produto = produtoService.findById(pesquisa.getProdutoId());
		
		PesquisaDePreco pesquisaDePreco = new PesquisaDePreco();
		pesquisaDePreco.setProduto(produto);
		pesquisaDePreco.setDataPesquisa(pesquisa.getDataPesquisa());
		pesquisaDePreco.setQuantidade(pesquisa.getQuantidade());
		
		List<Preco> precos = createPrecos(pesquisa, pesquisaDePreco);
		
		pesquisaDePreco.setPrecos(precos);
		BigDecimal precoMedio = pesquisaDePreco.precoMedio();
		pesquisaDePreco.setPrecoMedio(precoMedio);
		produto.setPrecoMedio(precoMedio);
		return repository.save(pesquisaDePreco);
	}

	private List<Preco> createPrecos(PesquisaDePrecoDTO pesquisaDePrecoDTO,
									 PesquisaDePreco pesquisaDePreco) {
		return pesquisaDePrecoDTO.getPrecos().stream()
				.map(preco -> {
					Preco p = new Preco();
                    p.setValor(Objects.requireNonNullElse(preco, BigDecimal.ZERO));
					p.setPesquisa(pesquisaDePreco);
					return p;
				})
				.collect(Collectors.toList());
	}
	
	public PesquisaDePreco update(Long id, PesquisaDePreco pesquisa) {
		PesquisaDePreco pesquisaTarget =
				repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pesquisa", id));
		updateData(pesquisa, pesquisaTarget);
		return repository.save(pesquisaTarget);
	}

	private void updateData(PesquisaDePreco pesquisa, PesquisaDePreco pesquisaDePreco) {
	    Produto produto = produtoService.findById(pesquisa.getProduto().getId());
	    pesquisaDePreco.setProduto(produto);
	    pesquisaDePreco.setDataPesquisa(pesquisa.getDataPesquisa());
		pesquisaDePreco.setQuantidade(pesquisa.getQuantidade());

		apllyUpadatesOnPrices(pesquisa, pesquisaDePreco);

	    BigDecimal precoMedio = pesquisaDePreco.precoMedio();
	    pesquisaDePreco.setPrecoMedio(precoMedio);
	    produto.setPrecoMedio(precoMedio);
	}

	private void apllyUpadatesOnPrices(PesquisaDePreco pesquisa, PesquisaDePreco pesquisaTarget) {
		List<Preco> precosExistentes = pesquisaTarget.getPrecos();
		precosExistentes.clear();

		pesquisa.getPrecos().forEach(precoDTO -> {
			Preco preco = new Preco();
			preco.setId(precoDTO.getId());

			preco.setValor(precoDTO.getValor());
			preco.setPesquisa(pesquisaTarget);
			precosExistentes.add(preco);
		});
	}
	
	public void deleteById(Long id) {
		PesquisaDePreco pesquisaDePreco = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
				"Pesquisa", id));

		Produto produto = produtoService.findById(pesquisaDePreco.getProduto().getId());
		produto.setPrecoMedio(BigDecimal.ZERO);
		repository.delete(pesquisaDePreco);
	}
}
