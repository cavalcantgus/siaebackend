package com.siae.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.siae.dto.PesquisaDePrecoDTO;
import com.siae.entities.PesquisaDePreco;
import com.siae.entities.Produto;
import com.siae.services.PesquisaDePrecoService;

@RestController
@RequestMapping("/public/pesquisas")
public class PesquisaDePrecoController {
	
	@Autowired
	private PesquisaDePrecoService service;
	
	@GetMapping
	private ResponseEntity<List<PesquisaDePreco>> findAll() {
		List<PesquisaDePreco> pesquisas = service.findAll();
		return ResponseEntity.ok().body(pesquisas);
	}
	
	// @GetMapping(/{id})
	
	@PostMapping("/pesquisa")
	public ResponseEntity<PesquisaDePreco> insert(@RequestBody PesquisaDePrecoDTO obj) {
		PesquisaDePreco pesquisa = service.insert(obj);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(pesquisa.getId()).toUri();
		
		return ResponseEntity.created(uri).body(pesquisa);
	}
	
	@PutMapping("/pesquisa/{id}") 
	public ResponseEntity<PesquisaDePreco> update(@PathVariable Long id, @RequestBody PesquisaDePreco obj) {
		PesquisaDePreco pesquisa = service.update(id, obj);
		return ResponseEntity.ok().body(pesquisa);
	}
}
