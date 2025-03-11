package com.siae.services;

import com.siae.entities.Contrato;
import com.siae.entities.Produtor;
import com.siae.repositories.ContratoRepository;
import com.siae.repositories.ProdutorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ProdutorService produtorService;

    @Autowired
    public ContratoService(ContratoRepository contratoRepository,
                           ProdutorService produtorService) {
        this.contratoRepository = contratoRepository;
        this.produtorService = produtorService;
    }

    public List<Contrato> findAll() {
        return contratoRepository.findAll();
    }

    public Contrato findById(Long id) {
        Optional<Contrato> contrato = contratoRepository.findById(id);
        return contrato.orElseThrow(() -> new EntityNotFoundException("Contrato não encontrado"));
    }

    public Contrato insert(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public Contrato update(Long id, Contrato contrato) {
        try {
            if(contratoRepository.existsById(id)) {
                Contrato contratoTarget = contratoRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Contrato não encontrado"));
                updateData(contrato, contratoTarget);
                return contratoRepository.save(contratoTarget);
            } else {
                throw new EntityNotFoundException("Contrato não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateData(Contrato contrato, Contrato contratoTarget) {
        Produtor produtor = produtorService.findById(contrato.getProdutor().getId());

        contratoTarget.setProdutor(produtor);
        contratoTarget.setContratante(contrato.getContratante());
        contratoTarget.setContratante(contrato.getContratante());
        contratoTarget.setNumeroContrato(contrato.getNumeroContrato());
        contratoTarget.setDataContratacao(contrato.getDataContratacao());
    }

    public void deleteById(Long id) {
        try {
            if(contratoRepository.existsById(id)) {
                contratoRepository.deleteById(id);
            } else {
                throw new EntityNotFoundException("Contrato não encontrado");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
