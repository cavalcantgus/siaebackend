package com.siae.services;

import com.siae.dto.ProdutoCronogramaDTO;
import com.siae.entities.Cronograma;
import com.siae.entities.DetalhesCronograma;
import com.siae.entities.Produto;
import com.siae.exception.ResourceNotFoundException;
import com.siae.repositories.DetalhesCronogramaRepository;
import com.siae.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalhesCronogramaService {

    private final DetalhesCronogramaRepository detalhesCronogramaRepository;
    private final ProdutoRepository produtoRepository;

    public DetalhesCronogramaService(DetalhesCronogramaRepository detalhesCronogramaRepository, ProdutoRepository produtoRepository) {
        this.detalhesCronogramaRepository = detalhesCronogramaRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<DetalhesCronograma> insertAll(ProdutoCronogramaDTO entrega, Cronograma cronograma) {
        Produto produto = produtoRepository.findById(entrega.getProdutoId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto", entrega.getProdutoId()));

        return entrega.getCronogramas().stream()
                .map(cronogramaRequest -> {
                    DetalhesCronograma detalhe = new DetalhesCronograma();
                    detalhe.setProduto(produto);
                    detalhe.setCronograma(cronograma);
                    detalhe.setDataEntrega(cronogramaRequest.getDataEntrega());
                    detalhe.setQuantidade(cronogramaRequest.getQuantidade());
                    detalhe.setTotal(cronogramaRequest.getQuantidade().multiply(produto.getPrecoMedio()));
                    return detalhe;
                })
                .toList();
    }
}
