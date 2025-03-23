package com.siae.controllers;

import com.siae.entities.Ata;
import com.siae.entities.Contratante;
import com.siae.entities.Contrato;
import com.siae.relatorios.AtaDaChamadaPDF;
import com.siae.services.AtaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/atas")
public class AtaController {

    private final AtaService ataService;
    private final AtaDaChamadaPDF ataDaChamadaPDF;

    @Autowired
    public AtaController(AtaService ataService, AtaDaChamadaPDF ataDaChamadaPDF) {
        this.ataService = ataService;
        this.ataDaChamadaPDF = ataDaChamadaPDF;
    }

    @GetMapping()
    public ResponseEntity<List<Ata>> findAll(){
        List<Ata> atas = ataService.findAll();
        return ResponseEntity.ok().body(atas);
    }

    @GetMapping("/ata/generate/{id}")
    public ResponseEntity<?> generatePdf(@PathVariable Long id) {
        Ata ata = ataService.findById(id);
        byte[] pdfBytes = ataDaChamadaPDF.createPdf(ata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment","ata.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);

    }

    @PostMapping("/ata")
    public ResponseEntity<Ata> insert(@RequestBody Ata obj) {
        Ata ata = ataService.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(ata.getId()).toUri();
        return ResponseEntity.created(uri).body(ata);
    }

    @PutMapping("/ata/{id}")
    public ResponseEntity<Ata> update(@PathVariable Long id, @RequestBody Ata obj) {
        Ata ata = ataService.update(id, obj);
        return ResponseEntity.ok().body(ata);
    }

    @DeleteMapping("/ata/{id}")
    public ResponseEntity<Ata> delete(@PathVariable Long id) {
        ataService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
