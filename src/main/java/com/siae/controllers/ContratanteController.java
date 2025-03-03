package com.siae.controllers;

import com.siae.entities.Contratante;
import com.siae.services.ContratanteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/contratantes")
public class ContratanteController {

    private final ContratanteService contratanteService;

    public ContratanteController(ContratanteService contratanteService) {
        this.contratanteService = contratanteService;
    }

    @GetMapping()
    public ResponseEntity<List<Contratante>> findAll() {
        List<Contratante> contratantes = contratanteService.findAll();
        return ResponseEntity.ok().body(contratantes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contratante> findById(Long id) {
        Contratante contratante = contratanteService.findById(id);
        return ResponseEntity.ok().body(contratante);
    }

    @PostMapping("/contratante")
    public  ResponseEntity<Contratante> insert(Contratante obj) {
        Contratante contratante = contratanteService.insert(obj);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(contratante.getId()).toUri();

        return ResponseEntity.created(uri).body(contratante);
    }
}
