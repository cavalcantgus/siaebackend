package com.siae.controllers;

import com.siae.dto.CronogramaDTO;
import com.siae.entities.Cronograma;
import com.siae.services.CronogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/cronogramas")
public class CronogramaController {

    private final CronogramaService cronogramaService;

    @Autowired
    public CronogramaController(CronogramaService cronogramaService) {
        this.cronogramaService = cronogramaService;
    }

    @GetMapping
    public ResponseEntity<List<Cronograma>> findAll() {
        List<Cronograma> cronogramas = cronogramaService.findAll();
        return ResponseEntity.ok().body(cronogramas);
    }

    @PostMapping("/cronograma")
    public ResponseEntity<CronogramaDTO> insert(@RequestBody CronogramaDTO cronogramaDTO) {
        Cronograma cronograma = cronogramaService.insert(cronogramaDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cronograma.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }
}
