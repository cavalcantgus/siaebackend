package com.siae.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.siae.dto.ProjetoDeVendaDTO;
import com.siae.entities.PesquisaDePreco;
import com.siae.entities.ProjetoDeVenda;
import com.siae.relatorios.RelatorioProjetoDeVendaPDF;
import com.siae.services.ProjetoDeVendaService;

@RestController
@RequestMapping("/public/projetos")
public class ProjetoDeVendaController {
	
	@Autowired
	private ProjetoDeVendaService service;
	
	@Autowired
	private RelatorioProjetoDeVendaPDF pdfService;
	
	@GetMapping()
	public ResponseEntity<List<ProjetoDeVenda>> findAll() {
		List<ProjetoDeVenda> projetos = service.findAll();
		return ResponseEntity.ok().body(projetos);
	}
	
	@GetMapping("/relatorio/generate/{id}")
	public ResponseEntity<?> generatePdf(@PathVariable Long id) {
		ProjetoDeVenda projeto = service.findById(id);
		byte[] pdfBytes = pdfService.createPdf(projeto);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "relatorio.pdf");
		
		return ResponseEntity.ok().headers(headers).body(pdfBytes);
		
	}
	
	@PostMapping("/projeto")
	public ResponseEntity<ProjetoDeVenda> insert(@RequestBody ProjetoDeVendaDTO projetoDTO) {
		ProjetoDeVenda projeto = service.insert(projetoDTO);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(projeto.getId()).toUri();
		
		return ResponseEntity.created(uri).body(projeto);
	}
	
	@PutMapping("/projeto/{id}") 
	public ResponseEntity<ProjetoDeVenda> update(@PathVariable Long id, @RequestBody ProjetoDeVenda obj) {
		ProjetoDeVenda projeto = service.update(id, obj);
		return ResponseEntity.ok().body(projeto);
	}

	@DeleteMapping("/projeto/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
