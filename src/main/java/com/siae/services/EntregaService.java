package com.siae.services;

import com.siae.dto.EntregaDTO;
import com.siae.entities.*;
import com.siae.repositories.DetalhesEntregaRepository;
import com.siae.repositories.EntregaRepository;
import com.siae.repositories.ProjetoDeVendaRepository;
import com.siae.repositories.ProjetoProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
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

        projetoDeVenda.setTotal(projetoDeVenda.getTotal().subtract(total));
        projetoDeVenda.setQuantidadeTotal(projetoDeVenda.getQuantidadeTotal().subtract(quantidade));

        projetoDeVendaRepository.save(projetoDeVenda);
        detalhesEntregaRepository.saveAll(detalhesEntregas);
        entrega.setDetalhesEntrega(detalhesEntregas);
        return entrega;
    }
}
