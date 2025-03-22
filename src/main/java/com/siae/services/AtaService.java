package com.siae.services;

import com.siae.entities.Ata;
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
}
