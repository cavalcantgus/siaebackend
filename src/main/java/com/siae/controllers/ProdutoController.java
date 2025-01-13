package com.siae.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.siae.entities.Produto;
import com.siae.services.ProdutoService;

@RestController
@RequestMapping("/public/produtos")
public class ProdutoController {
	
	@Autowired
	private ProdutoService service;
	
	@GetMapping
	public ResponseEntity<List<Produto>> findAll() {
		List<Produto> produtos = service.findAll();
		return ResponseEntity.ok().body(produtos);
	}
	
	// GetMapping("/{id}")
	
	@PostMapping("/produto") 
	public ResponseEntity<Produto> insert(@RequestBody Produto obj) {
		Produto produto = service.insert(obj);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(produto.getId()).toUri();
		
		return ResponseEntity.created(uri).body(produto);
	}
	
	@PutMapping("/produto/{id}") 
	public ResponseEntity<Produto> update(@PathVariable Long id, @RequestBody Produto obj) {
		Produto produto = service.update(id, obj);
		return ResponseEntity.ok().body(produto);
	}
	
	@DeleteMapping("/produto/{id}")
	public ResponseEntity<Produto> delete(@PathVariable Long id) {
		service.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
