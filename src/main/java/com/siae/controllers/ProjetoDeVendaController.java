package com.siae.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.siae.dto.ProjetoDeVendaDTO;
import com.siae.entities.ProjetoDeVenda;
import com.siae.services.ProjetoDeVendaService;

@RestController
@RequestMapping("/public/projetos")
public class ProjetoDeVendaController {
	
	@Autowired
	private ProjetoDeVendaService service;
	
	@GetMapping()
	public ResponseEntity<List<ProjetoDeVenda>> findAll() {
		List<ProjetoDeVenda> projetos = service.findAll();
		return ResponseEntity.ok().body(projetos);
	}
	
	@PostMapping("/projeto")
	public ResponseEntity<ProjetoDeVenda> insert(@RequestBody ProjetoDeVendaDTO projetoDTO) {
		ProjetoDeVenda projeto = service.insert(projetoDTO);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(projeto.getId()).toUri();
		
		return ResponseEntity.created(uri).body(projeto);
	}
}
