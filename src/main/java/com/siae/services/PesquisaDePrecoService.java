package com.siae.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.dto.PesquisaDePrecoDTO;
import com.siae.dto.UpdatePesquisaDePrecoDTO;
import com.siae.entities.PesquisaDePreco;
import com.siae.entities.Preco;
import com.siae.entities.Produto;
import com.siae.repositories.PesquisaDePrecoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PesquisaDePrecoService {

	@Autowired
	private PesquisaDePrecoRepository repository;

	@Autowired
	private ProdutoService produtoService;

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
		
		
		List<Preco> precos = pesquisa.getPreços().stream()
				.map(preco -> {
					Preco p = new Preco();
					if(preco == null) {
						p.setValor(BigDecimal.valueOf(0.0));
					}
					else {
						p.setValor(preco);
					}
					p.setPesquisa(pesquisaDePreco);
					return p;
				})
				.collect(Collectors.toList());
		
		pesquisaDePreco.setPrecos(precos);
		BigDecimal precoMedio = pesquisaDePreco.precoMedio();
		pesquisaDePreco.setPrecoMedio(precoMedio);
		pesquisaDePreco.setTotal(precoMedio.multiply(BigDecimal.valueOf(pesquisa.getQuantidade().longValue())));
		produto.setPrecoMedio(precoMedio);
		return repository.save(pesquisaDePreco);
	}
	
	public PesquisaDePreco update(Long id, PesquisaDePreco pesquisa) {	
		try {
			PesquisaDePreco pesquisaDePreco = repository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Pesquisa não encontrada"));
			updateData(pesquisa, pesquisaDePreco);
			return repository.save(pesquisaDePreco);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void updateData(PesquisaDePreco pesquisa, PesquisaDePreco pesquisaDePreco) {
		Produto produto = produtoService.findById(pesquisa.getProduto().getId());
		pesquisaDePreco.setProduto(produto);
		pesquisaDePreco.setDataPesquisa(pesquisa.getDataPesquisa());
		pesquisaDePreco.setQuantidade(pesquisa.getQuantidade());
		
		List<Preco> precosAtualizados = pesquisa.getPrecos().stream()
			    .map(precoDTO -> {
			        if (precoDTO == null) {
			            throw new IllegalArgumentException("Preço não pode ser nulo.");
			        }

			        Preco preco = new Preco();
			        preco.setId(precoDTO.getId());

			        // Verificar se o valor é nulo e lançar uma exceção ou definir um valor padrão
			        if (precoDTO.getValor() == null) {
			            throw new IllegalArgumentException("Valor do preço não pode ser nulo.");
			        }

			        preco.setValor(precoDTO.getValor());
			        preco.setPesquisa(pesquisaDePreco); 
			        return preco;
			    })
			    .collect(Collectors.toList());

		 
		pesquisaDePreco.setPrecos(precosAtualizados);
		BigDecimal precoMedio = pesquisaDePreco.precoMedio();
		pesquisaDePreco.setPrecoMedio(precoMedio);
		pesquisaDePreco.setTotal(precoMedio.multiply(BigDecimal.valueOf(pesquisa.getQuantidade().longValue())));
		produto.setPrecoMedio(precoMedio);
	}
}
