package com.siae.services;

import com.siae.dto.CronogramaDTO;
import com.siae.dto.ProdutoCronogramaDTO;
import com.siae.entities.Cronograma;
import com.siae.entities.DetalhesCronograma;
import com.siae.entities.Produtor;
import com.siae.exception.ResourceNotFoundException;
import com.siae.repositories.CronogramaRepository;
import com.siae.repositories.DetalhesCronogramaRepository;
import com.siae.repositories.ProdutoRepository;
import com.siae.repositories.ProdutorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CronogramaService {

    private final CronogramaRepository cronogramaRepository;
    private final DetalhesCronogramaService detalhesCronogramaService;
    private final ProdutorRepository produtorRepository;
    private final DetalhesCronogramaRepository detalhesCronogramaRepository;

    @Autowired
    public CronogramaService(CronogramaRepository cronogramaRepository,
                             DetalhesCronogramaService detalhesCronogramaService,
                             ProdutorRepository produtorRepository, DetalhesCronogramaRepository detalhesCronogramaRepository) {
        this.cronogramaRepository = cronogramaRepository;
        this.detalhesCronogramaService = detalhesCronogramaService;
        this.produtorRepository = produtorRepository;
        this.detalhesCronogramaRepository = detalhesCronogramaRepository;
    }

    public List<Cronograma> findAll() {
        return cronogramaRepository.findAll();
    }

    public List<Cronograma> findByProdutorId(Long produtorId) {
        return cronogramaRepository.findByProdutorId(produtorId);
    }

    public Cronograma findById(Long id) {
        return cronogramaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cronograma", id));
    }

    @Transactional
    public Cronograma insert(CronogramaDTO cronogramaDto) {
        Produtor produtor =
                produtorRepository.findById(cronogramaDto.getProdutorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produtor", cronogramaDto.getProdutorId()));
        Cronograma cronograma = new Cronograma();
        cronograma.setProdutor(produtor);
        cronograma.setMesReferente(cronogramaDto.getMesReferente());
        List<DetalhesCronograma> detalhesCronogramas  = new ArrayList<>();
        cronogramaRepository.save(cronograma);

        for(ProdutoCronogramaDTO produtoCronogramaDTO : cronogramaDto.getDetalhes()) {
            detalhesCronogramas.addAll(detalhesCronogramaService.insertAll(produtoCronogramaDTO,
                    cronograma));
        }
        BigDecimal total = cronograma.valorTotal(detalhesCronogramas);
        BigDecimal quantidade = cronograma.quantidadeTotal(detalhesCronogramas);
        cronograma.setTotal(total);
        cronograma.setQuantidade(quantidade);
        detalhesCronogramaRepository.saveAll(detalhesCronogramas);
        cronograma.setDetalhesCronograma(detalhesCronogramas);
        return cronograma;
    }
}
