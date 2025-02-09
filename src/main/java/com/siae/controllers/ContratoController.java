package com.siae.controllers;

import com.siae.entities.Contrato;
import com.siae.services.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    @Autowired
    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping
    public ResponseEntity<List<Contrato>> findAll() {
        List<Contrato> contratos = contratoService.findAll();
        return ResponseEntity.ok().body(contratos);
    }

    @PostMapping("/contrato")
    public ResponseEntity<Contrato> insert(@RequestBody  Contrato obj) {
        Contrato contrato = contratoService.insert(obj);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(contrato.getId()).toUri();
        return ResponseEntity.created(uri).body(contrato);
    }

    @PutMapping("/contrato/{id}")
    public ResponseEntity<Contrato> update(@PathVariable Long id, @RequestBody Contrato obj) {
        Contrato contrato = contratoService.update(id, obj);
        return ResponseEntity.ok().body(contrato);
    }
}
