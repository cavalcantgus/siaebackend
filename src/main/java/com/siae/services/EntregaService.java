package com.siae.services;

import com.siae.dto.EntregaDTO;
import com.siae.entities.*;
import com.siae.repositories.DetalhesEntregaRepository;
import com.siae.repositories.EntregaPagamentoRepository;
import com.siae.repositories.EntregaRepository;
import com.siae.repositories.ProjetoDeVendaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private final EntregaPagamentoRepository entregaPagamentoRepository;

    @Autowired
    public EntregaService(EntregaRepository entregaRepository,
                          ProdutorService produtorService,
                          ProdutoService produtoService,
                          ProjetoDeVendaService projetoDeVendaService,
                          ProjetoDeVendaRepository projetoDeVendaRepository,
                          DetalhesEntregaRepository detalhesEntregaRepository, EntregaPagamentoRepository entregaPagamentoRepository) {

        this.entregaRepository = entregaRepository;
        this.produtorService = produtorService;
        this.produtoService = produtoService;
        this.projetoDeVendaService = projetoDeVendaService;
        this.projetoDeVendaRepository = projetoDeVendaRepository;
        this.detalhesEntregaRepository = detalhesEntregaRepository;
        this.entregaPagamentoRepository = entregaPagamentoRepository;
    }

    ;

    public List<Entrega> findAll() {
        return entregaRepository.findAll();
    }

    public Entrega findById(Long id) {
        Optional<Entrega> entrega = entregaRepository.findById(id);
        return entrega.orElseThrow(() -> new EntityNotFoundException("Comprovante de Entrega não encontrado"));
    }

    public List<Entrega> findByProdutorId(Long id) {
        return entregaRepository.findByProdutorId(id);
    }

    public Entrega insert(EntregaDTO entregaDTO) {
        Produtor produtor = produtorService.findById(entregaDTO.getProdutorId());

        Entrega entrega = new Entrega();
        entrega.setProdutor(produtor);
        entrega.setDataDaEntrega(entregaDTO.getDataEntrega());
        entregaRepository.save(entrega);

        if (entregaDTO.getProdutoIds().size() != entregaDTO.getQuantidade().size()) {
            throw new IllegalArgumentException("A lista de produtos e a lista de quantidades devem ser iguais");
        }

        List<DetalhesEntrega> detalhesEntregas = new ArrayList<>();

        for (int i = 0; i < entregaDTO.getProdutoIds().size(); i++) {
            Produto produto = produtoService.findById(entregaDTO.getProdutoIds().get(i));

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
        Produtor produtor = produtorService.findById(payloadEntrega.getProdutor().getId());
        novaEntrega.setProdutor(produtor);
        novaEntrega.setDataDaEntrega(payloadEntrega.getDataDaEntrega());

        if (novaEntrega.getDetalhesEntrega().size() == 1 && payloadEntrega.getDetalhesEntrega().size() == 1) {
            handleSingleDetalhesEntrega(payloadEntrega, novaEntrega, novaEntrega.getDetalhesEntrega().get(0));
        } else {
            handleMultipleDetalhesEntrega(payloadEntrega, novaEntrega);
        }

        BigDecimal quantidadeTotal = novaEntrega.quantidadeTotal(novaEntrega.getDetalhesEntrega());
        BigDecimal total = novaEntrega.valorTotal(novaEntrega.getDetalhesEntrega());
        novaEntrega.setQuantidade(quantidadeTotal);
        novaEntrega.setTotal(total);

        detalhesEntregaRepository.saveAll(novaEntrega.getDetalhesEntrega());
        entregaRepository.save(novaEntrega);
    }

    private void handleMultipleDetalhesEntrega(Entrega payloadEntrega, Entrega novaEntrega) {
        payloadEntrega.getDetalhesEntrega().forEach((d) -> {
            System.out.println(d.getProduto().getDescricao());
        });

        novaEntrega.getDetalhesEntrega().removeIf(detalhesEntregaAnt -> {
            boolean shouldRemove = payloadEntrega.getDetalhesEntrega().stream()
                    .noneMatch(detalhesEntrega -> detalhesEntrega.getId().equals(detalhesEntregaAnt.getId()));

            System.out.println("Devo Remover: " + shouldRemove);
            if (shouldRemove) {
                detalhesEntregaRepository.delete(detalhesEntregaAnt);
            }

            return shouldRemove;
        });

        payloadEntrega.getDetalhesEntrega().forEach(detalhesEntrega -> {
            if (detalhesEntrega.getId() == null) {
                adicionarNovaEntrega(detalhesEntrega, novaEntrega);
            } else {
                DetalhesEntrega detalhesEntregaExistente = detalhesEntregaRepository.findById(detalhesEntrega.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Entrega não encontrada"));
                atualizarEntregaExistente(detalhesEntregaExistente, detalhesEntrega);
            }
        });
    }

    private void handleSingleDetalhesEntrega(Entrega payloadEntrega, Entrega novaEntrega, DetalhesEntrega detalhesEntrega) {
        payloadEntrega.getDetalhesEntrega().forEach(entrega -> {
            if (!entrega.getProduto().getId().equals(detalhesEntrega.getProduto().getId())) {

                detalhesEntregaRepository.delete(detalhesEntrega);

                adicionarNovaEntrega(entrega, novaEntrega);
                novaEntrega.getDetalhesEntrega().remove(0);
            } else {
                atualizarEntregaExistente(detalhesEntrega, entrega);
            }
        });
    }


    private void atualizarEntregaExistente(DetalhesEntrega detalhesEntrega, DetalhesEntrega entrega) {
        if (detalhesEntrega.getProduto().getId().equals(entrega.getProduto().getId())) {
            BigDecimal quantidadeAnterior = detalhesEntrega.getQuantidade();
            BigDecimal novaQuantidade = entrega.getQuantidade();
            Long produtorId = detalhesEntrega.getEntrega().getProdutor().getId();

            Produto produto = produtoService.findById(detalhesEntrega.getProduto().getId());
            ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(produtorId);

//            Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream()
//                    .filter(p -> p.getProduto().getId().equals(produto.getId()))
//                    .findFirst();

//            projetoProduto.ifPresent(pp -> {
//                pp.setQuantidade(pp.getQuantidade().add(quantidadeAnterior).subtract(novaQuantidade));
//                pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
//            });

            BigDecimal total = produto.getPrecoMedio().multiply(novaQuantidade);
            detalhesEntrega.setQuantidade(novaQuantidade);
            detalhesEntrega.setProduto(produto);
            detalhesEntrega.setTotal(total);
        } else {
            Produto produto = produtoService.findById(entrega.getProduto().getId());

//            ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(entrega.getEntrega().getProdutor().getId());

//            Optional<ProjetoProduto> projetoProduto = projetoDeVenda.getProjetoProdutos().stream()
//                    .filter(p -> p.getProduto().getId().equals(produto.getId()))
//                    .findFirst();
//
//            projetoProduto.ifPresent(pp -> {
//                pp.setQuantidade(pp.getQuantidade().subtract(entrega.getQuantidade()));
//                pp.setTotal(pp.getQuantidade().multiply(produto.getPrecoMedio()));
//            });

            BigDecimal total = produto.getPrecoMedio().multiply(entrega.getQuantidade());
            detalhesEntrega.setTotal(total);
            detalhesEntrega.setProduto(produto);
            detalhesEntrega.setQuantidade(entrega.getQuantidade());
        }
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

    @Transactional
    public void desassociarEntregaDePagamento(Long id) {
        try {
            if (entregaRepository.existsById(id)) {
                EntregaPagamento entregaPagamento = entregaPagamentoRepository.findByEntregaId(id).orElseThrow(() -> new EntityNotFoundException("EntregaPagamento não encontrada"));
                BigDecimal quantidadeSubtract = entregaPagamento.getEntrega().getQuantidade();
                BigDecimal valueSubtract = entregaPagamento.getEntrega().getTotal();
                entregaPagamento.getPagamento().setQuantidade(entregaPagamento.getPagamento().getQuantidade().subtract(quantidadeSubtract));
                entregaPagamento.getPagamento().setTotal(entregaPagamento.getPagamento().getTotal().subtract(valueSubtract));

                entregaPagamento.getEntrega().setEnviadoParaPagamento(false);

                entregaPagamentoRepository.delete(entregaPagamento);

            } else {
                throw new EntityNotFoundException("Entrega não encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(Long id) {
        try {
            if (id != null && entregaRepository.existsById(id)) {
                entregaRepository.deleteById(id);
            } else {
                throw new EntityNotFoundException("Entrega não encontrada");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
