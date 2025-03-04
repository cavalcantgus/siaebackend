package com.siae.services;

import com.siae.entities.Contratante;
import com.siae.repositories.ContratanteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContratanteService {

    private final ContratanteRepository contratanteRepository;

    public ContratanteService(ContratanteRepository contratanteRepository) {
        this.contratanteRepository = contratanteRepository;
    }

    public List<Contratante> findAll() {
        return contratanteRepository.findAll();
    }

    public Contratante findById(Long id) {
        Optional<Contratante> contratante = contratanteRepository.findById(id);
        return  contratante.orElseThrow(() -> new EntityNotFoundException("Contratante não " +
                "encontrado"));
    }

    public Contratante insert(Contratante contratante) {
        return contratanteRepository.save(contratante);
    }

    public Contratante update(Long id, Contratante contratante) {
        try {
            if(contratanteRepository.existsById(id)) {
                Contratante contratanteTarget = contratanteRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Contratante não encontrado"));
                updateData(contratante, contratanteTarget);
                return contratanteRepository.save(contratanteTarget);
            } else {
                throw new EntityNotFoundException("Contratante não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateData(Contratante contratante, Contratante contratanteTarget) {
        contratanteTarget.setNome(contratanteTarget.getNome());
        contratanteTarget.setCpf(contratanteTarget.getCpf());
    }
}
