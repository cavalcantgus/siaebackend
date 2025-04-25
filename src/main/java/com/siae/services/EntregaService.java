package com.siae.services;

import com.siae.dto.EntregaDTO;
import com.siae.entities.*;
import com.siae.repositories.DetalhesEntregaRepository;
import com.siae.repositories.EntregaPagamentoRepository;
import com.siae.repositories.EntregaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final ProdutorService produtorService;
    private final ProdutoService produtoService;
    private final DetalhesEntregaRepository detalhesEntregaRepository;
    private final EntregaPagamentoRepository entregaPagamentoRepository;

    @Autowired
    public EntregaService(EntregaRepository entregaRepository,
                          ProdutorService produtorService,
                          ProdutoService produtoService,
                          DetalhesEntregaRepository detalhesEntregaRepository, EntregaPagamentoRepository entregaPagamentoRepository) {

        this.entregaRepository = entregaRepository;
        this.produtorService = produtorService;
        this.produtoService = produtoService;
        this.detalhesEntregaRepository = detalhesEntregaRepository;
        this.entregaPagamentoRepository = entregaPagamentoRepository;
    }


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

        List<DetalhesEntrega> detalhesEntregas = mapDetalhesEntrega(entregaDTO, entrega);

        BigDecimal total = entrega.valorTotal(detalhesEntregas);
        BigDecimal quantidade = entrega.quantidadeTotal(detalhesEntregas);
        entrega.setTotal(total);
        entrega.setQuantidade(quantidade);

        detalhesEntregaRepository.saveAll(detalhesEntregas);
        entrega.setDetalhesEntrega(detalhesEntregas);
        return entrega;
    }

    public List<DetalhesEntrega> mapDetalhesEntrega(EntregaDTO entregaDTO, Entrega entrega) {
        return IntStream.range(0, entregaDTO.getProdutoIds().size())
                .mapToObj(i -> {
                    Produto produto = produtoService.findById(entregaDTO.getProdutoIds().get(i));

                    DetalhesEntrega detalhesEntrega = new DetalhesEntrega();
                    detalhesEntrega.setProduto(produto);
                    detalhesEntrega.setQuantidade(entregaDTO.getQuantidade().get(i));
                    detalhesEntrega.setTotal(entregaDTO.getQuantidade().get(i).multiply(produto.getPrecoMedio()));
                    detalhesEntrega.setEntrega(entrega);
                    return detalhesEntrega;
                }).toList();
    }


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
        novaEntrega.getDetalhesEntrega().removeIf(detalhesEntregaAnt -> {
            boolean shouldRemove = payloadEntrega.getDetalhesEntrega().stream()
                    .noneMatch(detalhesEntrega -> detalhesEntrega.getId().equals(detalhesEntregaAnt.getId()));

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
            BigDecimal novaQuantidade = entrega.getQuantidade();
            Produto produto = produtoService.findById(detalhesEntrega.getProduto().getId());

            BigDecimal total = produto.getPrecoMedio().multiply(novaQuantidade);
            detalhesEntrega.setQuantidade(novaQuantidade);
            detalhesEntrega.setTotal(total);
        } else {
            Produto produto = produtoService.findById(entrega.getProduto().getId());

            BigDecimal total = produto.getPrecoMedio().multiply(entrega.getQuantidade());
            detalhesEntrega.setTotal(total);
            detalhesEntrega.setProduto(produto);
            detalhesEntrega.setQuantidade(entrega.getQuantidade());
        }
    }

    private void adicionarNovaEntrega(DetalhesEntrega entrega, Entrega novaEntrega) {
        Produto produto = produtoService.findById(entrega.getProduto().getId());

        BigDecimal total = entrega.getQuantidade().multiply(produto.getPrecoMedio());

        DetalhesEntrega novoDetalhesEntrega = new DetalhesEntrega();
        novoDetalhesEntrega.setProduto(produto);
        novoDetalhesEntrega.setQuantidade(entrega.getQuantidade());
        novoDetalhesEntrega.setTotal(total);
        novoDetalhesEntrega.setEntrega(novaEntrega);

        novaEntrega.getDetalhesEntrega().add(novoDetalhesEntrega);
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
