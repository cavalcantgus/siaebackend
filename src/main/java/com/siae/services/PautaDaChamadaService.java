package com.siae.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.entities.PautaDaChamada;
import com.siae.repositories.PautaDaChamadaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PautaDaChamadaService {
	
	@Autowired
	private PautaDaChamadaRepository repository;
	
	public List<PautaDaChamada> findAll() {
		return repository.findAll();
	}
	
	public PautaDaChamada findById(Long id) {
		Optional<PautaDaChamada> pauta = repository.findById(id);
		return pauta.orElseThrow(() -> new EntityNotFoundException("Registro não encontrado"));
	}
	
	public PautaDaChamada insert(PautaDaChamada pauta) {
		return repository.save(pauta);
	}
	
	public PautaDaChamada update(Long id, PautaDaChamada pauta) {
		try {
			PautaDaChamada pautaTarget = repository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Registro não encontrado"));
			updateData(pauta, pautaTarget);
			return repository.save(pautaTarget);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void updateData(PautaDaChamada pauta, PautaDaChamada pautaTarget) {
		pautaTarget.setQuantidade(pauta.getQuantidade());
	}
}
