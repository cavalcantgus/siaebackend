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

import com.siae.entities.PautaDaChamada;
import com.siae.services.PautaDaChamadaService;

@RestController
@RequestMapping("/public/pautas")
public class PautaDaChamadaController {
	
	@Autowired
	private PautaDaChamadaService service;
	
	@GetMapping
	public ResponseEntity<List<PautaDaChamada>> findAll() {
		List<PautaDaChamada> pautas = service.findAll();
		return ResponseEntity.ok().body(pautas);
	}
	
	@PostMapping("/pauta")
	public ResponseEntity<PautaDaChamada> insert(@RequestBody PautaDaChamada obj) {
		PautaDaChamada pauta = service.insert(obj);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(pauta.getId()).toUri();
		
		return ResponseEntity.created(uri).body(pauta);
	}
	
	@PutMapping("/pauta/{id}")
	public ResponseEntity<PautaDaChamada> update(@PathVariable Long id, @RequestBody PautaDaChamada obj) {
		PautaDaChamada pauta = service.update(id, obj);
		return ResponseEntity.ok().body(pauta);
	}
}
