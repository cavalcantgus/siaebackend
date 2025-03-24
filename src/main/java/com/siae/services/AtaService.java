package com.siae.services;

import com.siae.entities.Ata;
import com.siae.entities.Contrato;
import com.siae.entities.Produtor;
import com.siae.repositories.AtaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AtaService {

    private final AtaRepository ataRepository;

    @Autowired
    public AtaService(AtaRepository ataRepository) {
        this.ataRepository = ataRepository;
    }

    public List<Ata> findAll() {
        return ataRepository.findAll();
    }

    public Ata findById(Long id) {
        Optional<Ata> ata = ataRepository.findById(id);
        return ata.orElseThrow(() -> new EntityNotFoundException("Ata não encontrada"));
    }

    public Ata insert(Ata ata) {
        return ataRepository.save(ata);
    }

    public Ata update(Long id, Ata ata) {
        try {
            if(ataRepository.existsById(id)) {
                Ata ataTarget = ataRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Contrato não encontrado"));
                updateData(ata, ataTarget);
                return ataRepository.save(ataTarget);
            } else {
                throw new EntityNotFoundException("Contrato não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateData(Ata ata, Ata ataTarget) {
        ataTarget.setHora(ata.getHora());
        ataTarget.setPrefeito(ata.getPrefeito());
        ataTarget.setPresidente(ata.getPresidente());
        ataTarget.setNutricionista(ata.getNutricionista());
        ataTarget.setSecCpl(ata.getSecCpl());
        ataTarget.setSecEduc(ata.getSecEduc());
        ataTarget.setData(ata.getData());
        ataTarget.setMembros(ata.getMembros());
    }

    public void deleteById(Long id) {
        try {
            if(ataRepository.existsById(id)) {
                ataRepository.deleteById(id);
            } else {
                throw new EntityNotFoundException("Ata não encontrada");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
