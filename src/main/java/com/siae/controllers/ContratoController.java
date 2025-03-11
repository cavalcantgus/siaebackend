package com.siae.controllers;

import com.siae.entities.Contratante;
import com.siae.entities.Contrato;
import com.siae.entities.Entrega;
import com.siae.relatorios.ContratoPDF;
import com.siae.services.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/contratos")
public class ContratoController {

    private final ContratoService contratoService;
    private final ContratoPDF contratoPDF;

    @Autowired
    public ContratoController(ContratoService contratoService,
                              ContratoPDF contratoPDF) {
        this.contratoService = contratoService;
        this.contratoPDF = contratoPDF;
    }

    @GetMapping
    public ResponseEntity<List<Contrato>> findAll() {
        List<Contrato> contratos = contratoService.findAll();
        return ResponseEntity.ok().body(contratos);
    }

    @GetMapping("/contrato/generate/{id}")
    public ResponseEntity<?> generateContratoPDF(@PathVariable Long id) {
        Contrato contrato = contratoService.findById(id);
        byte[] pdfBytes = contratoPDF.createPdf(contrato);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "contrato_" + contrato.getProdutor().getNome() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
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

    @DeleteMapping("/contrato/{id}")
    public ResponseEntity<Contratante> delete(@PathVariable Long id) {
        contratoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
