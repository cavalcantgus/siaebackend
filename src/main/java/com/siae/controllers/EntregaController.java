package com.siae.controllers;

import com.siae.dto.EntregaDTO;
import com.siae.entities.Entrega;
import com.siae.services.EntregaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/comprovantes")
public class EntregaController {

    private final EntregaService entregaService;

    @Autowired
    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public ResponseEntity<List<Entrega>> findAll() {
        List<Entrega> entregas = entregaService.findAll();
        return ResponseEntity.ok().body(entregas);
    }

    @PostMapping("/comprovante")
    public ResponseEntity<Entrega> insert(@RequestBody EntregaDTO entregaDTO) {
        Entrega entrega = entregaService.insert(entregaDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entrega.getId()).toUri();
        return ResponseEntity.created(uri).body(entrega);
    }
}
