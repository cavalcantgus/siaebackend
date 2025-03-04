package com.siae.controllers;

import com.siae.entities.Contratante;
import com.siae.services.ContratanteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public  ResponseEntity<Contratante> insert(@RequestBody Contratante obj) {
        Contratante contratante = contratanteService.insert(obj);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(contratante.getId()).toUri();

        return ResponseEntity.created(uri).body(contratante);
    }

    @PutMapping("/contratante/{id}")
    public ResponseEntity<Contratante> update(@PathVariable Long id, @RequestBody Contratante obj) {
        Contratante contratante = contratanteService.update(id, obj);
        return ResponseEntity.ok().body(contratante);
    }
}
