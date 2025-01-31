package com.siae.services;

import com.siae.dto.EntregaDTO;
import com.siae.entities.*;
import com.siae.repositories.DetalhesEntregaRepository;
import com.siae.repositories.EntregaRepository;
import com.siae.repositories.ProjetoDeVendaRepository;
import com.siae.repositories.ProjetoProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final ProdutorService produtorService;
    private final ProdutoService produtoService;
    private final ProjetoDeVendaService projetoDeVendaService;
    private final ProjetoDeVendaRepository projetoDeVendaRepository;
    private final DetalhesEntregaRepository detalhesEntregaRepository;

    @Autowired
    public EntregaService(EntregaRepository entregaRepository,
                          ProdutorService produtorService,
                          ProdutoService produtoService,
                          ProjetoDeVendaService projetoDeVendaService,
                          ProjetoDeVendaRepository projetoDeVendaRepository,
                          DetalhesEntregaRepository detalhesEntregaRepository) {

        this.entregaRepository = entregaRepository;
        this.produtorService = produtorService;
        this.produtoService = produtoService;
        this.projetoDeVendaService = projetoDeVendaService;
        this.projetoDeVendaRepository = projetoDeVendaRepository;
        this.detalhesEntregaRepository = detalhesEntregaRepository;
    };

    public List<Entrega> findAll() {
        return entregaRepository.findAll();
    }

    public Entrega findById(Long id) {
        Optional<Entrega> entrega = entregaRepository.findById(id);
        return entrega.orElseThrow(() -> new EntityNotFoundException("Comprovante de Entrega não encontrado"));
    }

    public Entrega insert(EntregaDTO entregaDTO) {
        Produtor produtor = produtorService.findById(entregaDTO.getProdutorId());
        ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(produtor.getId());
        Entrega entrega = new Entrega();
        entrega.setProdutor(produtor);
        entrega.setDataDaEntrega(entregaDTO.getDataEntrega());
        entregaRepository.save(entrega);

        if(entregaDTO.getProdutoIds().size() != entregaDTO.getQuantidade().size()) {
            throw new IllegalArgumentException("A lista de produtos e a lista de quantidades devem ser iguais");
        }

        List<DetalhesEntrega> detalhesEntregas = new ArrayList<>();

        for(int i = 0; i < entregaDTO.getProdutoIds().size(); i++) {
            Produto produto = produtoService.findById(entregaDTO.getProdutoIds().get(i));
            Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream().filter(projeto -> projeto.getProduto().getId().equals(produto.getId())).findFirst();
            int finalI = i;
            projetoProduto.ifPresent(pp -> {
                pp.setQuantidade(pp.getQuantidade().subtract(entregaDTO.getQuantidade().get(finalI)));
                pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
            });

            // Inserir nova instância de DetalhesEntrega
            DetalhesEntrega detalhesEntrega = new DetalhesEntrega();
            detalhesEntrega.setProduto(produto);
            detalhesEntrega.setQuantidade(entregaDTO.getQuantidade().get(i));
            detalhesEntrega.setTotal(entregaDTO.getQuantidade().get(i).multiply(produto.getPrecoMedio()));
            detalhesEntrega.setEntrega(entrega);
            detalhesEntregas.add(detalhesEntrega);
        }

        // Total e quantidade da entrega
        BigDecimal total = entrega.valorTotal(detalhesEntregas);
        BigDecimal quantidade = entrega.quantidadeTotal(detalhesEntregas);
        entrega.setTotal(total);
        entrega.setQuantidade(quantidade);

        projetoDeVendaRepository.save(projetoDeVenda);
        detalhesEntregaRepository.saveAll(detalhesEntregas);
        entrega.setDetalhesEntrega(detalhesEntregas);
        return entrega;
    }

    /*
    Id
    Produtor
    DataDaEntrega
    detalhesEntrega
    Total
    Quantidade
     */

    @Transactional
    public Entrega update(Long id, Entrega entrega) {
        try {
            Entrega novaEntrega = entregaRepository.findById(entrega.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Entrega não encontrada"));
            updateData(entrega, novaEntrega);
            return entregaRepository.save(novaEntrega);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    protected void updateData(Entrega payloadEntrega, Entrega novaEntrega) {
        Boolean mesmoProdutor = payloadEntrega.getProdutor().getId().equals(novaEntrega.getProdutor().getId());

        if(!payloadEntrega.getProdutor().getId().equals(novaEntrega.getProdutor().getId())){
            handleNewProducer(payloadEntrega, novaEntrega);
        }

        if(novaEntrega.getDetalhesEntrega().size() == 1 && payloadEntrega.getDetalhesEntrega().size() == 1){
            handleSingleDetalhesEntrega(payloadEntrega, novaEntrega, novaEntrega.getDetalhesEntrega().get(0), mesmoProdutor);
        } else {
            handleMultipleDetalhesEntrega(payloadEntrega, novaEntrega, mesmoProdutor);
        }

        BigDecimal quantidadeTotal = novaEntrega.quantidadeTotal(novaEntrega.getDetalhesEntrega());
        BigDecimal total = novaEntrega.valorTotal(novaEntrega.getDetalhesEntrega());
        novaEntrega.setQuantidade(quantidadeTotal);
        novaEntrega.setTotal(total);

        detalhesEntregaRepository.saveAll(novaEntrega.getDetalhesEntrega());
        entregaRepository.save(novaEntrega);
    }

    private void handleMultipleDetalhesEntrega(Entrega payloadEntrega, Entrega novaEntrega, Boolean condicao) {
        payloadEntrega.getDetalhesEntrega().forEach((d) -> {
            System.out.println(d.getProduto().getDescricao());
        });

        novaEntrega.getDetalhesEntrega().removeIf(detalhesEntregaAnt -> {
            boolean shouldRemove = payloadEntrega.getDetalhesEntrega().stream()
                    .noneMatch(detalhesEntrega -> detalhesEntrega.getId().equals(detalhesEntregaAnt.getId()));

            System.out.println("Devo Remover: " + shouldRemove);
            if(shouldRemove){
                if(condicao) {
                    devolverQuantidadeAoEstoque(detalhesEntregaAnt);
                }
                detalhesEntregaRepository.delete(detalhesEntregaAnt);
            }

            return shouldRemove;
        });

        payloadEntrega.getDetalhesEntrega().forEach(detalhesEntrega -> {
            if(detalhesEntrega.getId() == null) {
                adicionarNovaEntrega(detalhesEntrega, novaEntrega);
            } else {
                DetalhesEntrega detalhesEntregaExistente = detalhesEntregaRepository.findById(detalhesEntrega.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Entrega não encontrada"));
                atualizarEntregaExistente(detalhesEntregaExistente, detalhesEntrega);
            }
        });
    }

    private void handleNewProducer(Entrega payloadEntrega, Entrega novaEntrega) {
        // Buscando antigo produtor associado
        Produtor produtorAnt = produtorService.findById(novaEntrega.getProdutor().getId());

        // Buscando projeto de venda associado ao antigo produtor
        ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(produtorAnt.getId());

        // Devolvendo valores e quantidades aos ProjetoProdutos associados ao antigo Produtor
        novaEntrega.getDetalhesEntrega().forEach(entrega -> {
            Produto produto = produtoService.findById(entrega.getProduto().getId());
            Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream()
                    .filter(p -> p.getProduto().getId().equals(produto.getId())).findFirst();

            projetoProduto.ifPresent(pp -> {
                pp.setQuantidade(pp.getQuantidade().add(entrega.getQuantidade()));
                pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
            });
        });

        // Novo Produtor vindo do payload
        Produtor novoProdutor = produtorService.findById(payloadEntrega.getProdutor().getId());

        projetoDeVendaRepository.save(projetoDeVenda);
        novaEntrega.setProdutor(novoProdutor);
    }

    private void handleSingleDetalhesEntrega(Entrega payloadEntrega, Entrega novaEntrega, DetalhesEntrega detalhesEntrega, Boolean condicao) {
        payloadEntrega.getDetalhesEntrega().forEach(entrega -> {
            if(!entrega.getProduto().getId().equals(detalhesEntrega.getProduto().getId())){

                if(condicao) {
                    devolverQuantidadeAoEstoque(detalhesEntrega);
                }

                detalhesEntregaRepository.delete(detalhesEntrega);

                adicionarNovaEntrega(entrega, novaEntrega);
                novaEntrega.getDetalhesEntrega().remove(0);
            } else {
                atualizarEntregaExistente(detalhesEntrega, entrega);
            }
        });
    }

    private void devolverQuantidadeAoEstoque(DetalhesEntrega detalhesEntrega) {
        Produto produto = produtoService.findById(detalhesEntrega.getProduto().getId());
        Long produtorId = detalhesEntrega.getProduto().getId();
        ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(produtorId);

        Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream()
                .filter(p -> p.getProduto().getId().equals(produto.getId()))
                .findFirst();

        projetoProduto.ifPresent(pp -> {
            pp.setQuantidade(pp.getQuantidade().add(detalhesEntrega.getQuantidade()));
            pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
        });
    }

    private void atualizarEntregaExistente(DetalhesEntrega detalhesEntrega, DetalhesEntrega entrega) {
        BigDecimal quantidadeAnterior = detalhesEntrega.getQuantidade();
        BigDecimal novaQuantidade = entrega.getQuantidade();
        Long produtorId = detalhesEntrega.getEntrega().getProdutor().getId();

        Produto produto = produtoService.findById(detalhesEntrega.getProduto().getId());
        ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(produtorId);

        Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream()
                .filter(p -> p.getProduto().getId().equals(produto.getId()))
                .findFirst();

        projetoProduto.ifPresent(pp -> {
            pp.setQuantidade(pp.getQuantidade().add(quantidadeAnterior).subtract(novaQuantidade));
            pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
        });

        BigDecimal total = produto.getPrecoMedio().multiply(novaQuantidade);
        detalhesEntrega.setQuantidade(novaQuantidade);
        detalhesEntrega.setProduto(produto);
        detalhesEntrega.setTotal(total);
    }

    private void adicionarNovaEntrega(DetalhesEntrega entrega, Entrega novaEntrega) {
        Produto produto = produtoService.findById(entrega.getProduto().getId());
        ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(novaEntrega.getProdutor().getId());
        Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream()
                .filter(p -> p.getProduto().getId().equals(produto.getId()))
                .findFirst();
        projetoProduto.ifPresent(pp -> {
            pp.setQuantidade(pp.getQuantidade().subtract(entrega.getQuantidade()));
            pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
        });

        BigDecimal total = entrega.getQuantidade().multiply(produto.getPrecoMedio());

        DetalhesEntrega novoDetalhesEntrega = new DetalhesEntrega();
        novoDetalhesEntrega.setProduto(produto);
        novoDetalhesEntrega.setQuantidade(entrega.getQuantidade());
        novoDetalhesEntrega.setTotal(total);
        novoDetalhesEntrega.setEntrega(novaEntrega);

        novaEntrega.getDetalhesEntrega().add(novoDetalhesEntrega);
        projetoDeVendaRepository.save(projetoDeVenda);
    }
}
