package com.siae.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.siae.exception.ResourceNotFoundException;
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
import jakarta.transaction.Transactional;

@Service
public class ProjetoDeVendaService {

    private final ProjetoDeVendaRepository repository;
    private final ProdutorRepository produtorRepository;
    private final ProdutoRepository produtoRepository;
    private final ProjetoProdutoRepository projetoProdutoRepository;
    private final PesquisaDePrecoRepository pesquisaRepository;

    @Autowired
    public ProjetoDeVendaService(ProjetoDeVendaRepository repository,
                                 ProdutorRepository produtorRepository,
                                 ProdutoRepository produtoRepository,
                                 ProjetoProdutoRepository projetoProdutoRepository,
                                 PesquisaDePrecoRepository pesquisaRepository) {
        this.repository = repository;
        this.produtorRepository = produtorRepository;
        this.produtoRepository = produtoRepository;
        this.projetoProdutoRepository = projetoProdutoRepository;
        this.pesquisaRepository = pesquisaRepository;
    }

    public List<ProjetoDeVenda> findAll() {
        return repository.findAll();
    }

    public ProjetoDeVenda findById(Long id) {
        Optional<ProjetoDeVenda> projeto = repository.findById(id);
        return projeto.orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));
    }

    public ProjetoDeVenda findByProdutorId(Long produtorId) {
        return repository.findByProdutorId(produtorId);
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
        ProjetoDeVenda projetoDeVenda = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
        updateData(projeto, projetoDeVenda);
        return repository.save(projetoDeVenda);


    }

    @Transactional
    protected void updateData(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda) {
        Produtor produtor = produtorRepository.findById(projeto.getProdutor().getId())
                .orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
        projetoDeVenda.setProdutor(produtor);
        projetoDeVenda.setDataProjeto(projeto.getDataProjeto());

        List<ProjetoProduto> projetoProdutosExistentes = projetoDeVenda.getProjetoProdutos();

        handleMultipleProjetoProdutos(projeto, projetoDeVenda, projetoProdutosExistentes);

        BigDecimal quantidadeTotal = projetoDeVenda.quantidadeTotal(projetoDeVenda.getProjetoProdutos());
        BigDecimal total = projetoDeVenda.total(projetoDeVenda.getProjetoProdutos());
        projetoDeVenda.setQuantidadeTotal(quantidadeTotal);
        projetoDeVenda.setTotal(total);

        projetoProdutoRepository.saveAll(projetoDeVenda.getProjetoProdutos());
        projetoDeVenda.setProjetoProdutos(projetoDeVenda.getProjetoProdutos());
    }

    private void handleMultipleProjetoProdutos(ProjetoDeVenda projeto, ProjetoDeVenda projetoDeVenda, List<ProjetoProduto> projetoProdutosExistentes) {
        projetoProdutosExistentes.removeIf(projetoProdAnt -> {
            boolean shouldRemove = projeto.getProjetoProdutos().stream()
                    .noneMatch(projetoProd -> projetoProd.getId().equals(projetoProdAnt.getId()));

            if (shouldRemove) {
                projetoProdutoRepository.delete(projetoProdAnt);
            }

            return shouldRemove;
        });

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

    private void adicionarNovoProjetoProduto(ProjetoProduto projetoProduto, ProjetoDeVenda projetoDeVenda) {
        Produto produto = produtoRepository.findById(projetoProduto.getProduto().getId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        ProjetoProduto novoProjetoProduto = createNewProjetoProduto(projetoProduto, projetoDeVenda,
                produto);

        projetoDeVenda.getProjetoProdutos().add(novoProjetoProduto);
        for (ProjetoProduto p : projetoDeVenda.getProjetoProdutos()) {
            System.out.println("Projeto: " + p.getId());
        }
        System.out.println("Novo produto adicionado: " + novoProjetoProduto.getId());
    }

    private static ProjetoProduto createNewProjetoProduto(ProjetoProduto projetoProduto,
                                                          ProjetoDeVenda projetoDeVenda, Produto produto) {
        ProjetoProduto novoProjetoProduto = new ProjetoProduto();

        BigDecimal total = novoProjetoProduto.calculateTotal(produto.getPrecoMedio(),
                projetoProduto.getQuantidade());

        novoProjetoProduto.setProduto(produto);
        novoProjetoProduto.setQuantidade(projetoProduto.getQuantidade());
        novoProjetoProduto.setTotal(total);
        novoProjetoProduto.setInicioEntrega(projetoProduto.getInicioEntrega());
        novoProjetoProduto.setFimEntrega(projetoProduto.getFimEntrega());
        novoProjetoProduto.setProjeto(projetoDeVenda);
        return novoProjetoProduto;
    }

    private void atualizarProjetoProdutoExistente(ProjetoProduto projetoProdutoExistente, ProjetoProduto projetoProdutoAtualizado) {

        if (projetoProdutoExistente.getProduto().getId().equals(projetoProdutoAtualizado.getProduto().getId())) {
            BigDecimal novaQuantidade = projetoProdutoAtualizado.getQuantidade();

            Produto produto = produtoRepository.findById(projetoProdutoExistente.getProduto().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            BigDecimal total = projetoProdutoExistente.calculateTotal(produto.getPrecoMedio(), novaQuantidade);

            projetoProdutoExistente.setInicioEntrega(projetoProdutoAtualizado.getInicioEntrega());
            projetoProdutoExistente.setFimEntrega(projetoProdutoAtualizado.getFimEntrega());
            projetoProdutoExistente.setQuantidade(novaQuantidade);
            projetoProdutoExistente.setTotal(total);
        } else {

            Produto produto = produtoRepository.findById(projetoProdutoAtualizado.getProduto().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            BigDecimal total = projetoProdutoExistente.calculateTotal(produto.getPrecoMedio(),
                    projetoProdutoAtualizado.getQuantidade());

            projetoProdutoExistente.setProduto(produto);
            projetoProdutoExistente.setQuantidade(projetoProdutoAtualizado.getQuantidade());
            projetoProdutoExistente.setTotal(total);
            projetoProdutoAtualizado.setInicioEntrega(projetoProdutoAtualizado.getInicioEntrega());
            projetoProdutoAtualizado.setFimEntrega(projetoProdutoAtualizado.getFimEntrega());

        }
    }

    public void deleteById(Long id) {
        ProjetoDeVenda projeto = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
        repository.delete(projeto);
    }
}
