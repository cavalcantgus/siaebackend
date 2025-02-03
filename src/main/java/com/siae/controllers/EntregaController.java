package com.siae.controllers;

import com.siae.dto.EntregaDTO;
import com.siae.entities.Entrega;
import com.siae.entities.ProjetoDeVenda;
import com.siae.relatorios.ComprovanteDeRecebimento;
import com.siae.relatorios.EntregaMensal;
import com.siae.services.EntregaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/public/comprovantes")
public class EntregaController {

    private final EntregaService entregaService;
    private final ComprovanteDeRecebimento comprovanteService;
    private final EntregaMensal entregaMensalService;

    @Autowired
    public EntregaController(EntregaService entregaService,
                             ComprovanteDeRecebimento comprovanteService,
                             EntregaMensal entregaMensalService) {

        this.entregaService = entregaService;
        this.comprovanteService = comprovanteService;
        this.entregaMensalService = entregaMensalService;
    }

    @GetMapping
    public ResponseEntity<List<Entrega>> findAll() {
        List<Entrega> entregas = entregaService.findAll();
        return ResponseEntity.ok().body(entregas);
    }

    @GetMapping("relatorio/generate/{id}")
    public ResponseEntity<?> generatePdf(@PathVariable Long id) {
        Entrega entrega = entregaService.findById(id);
        byte[] pdfBytes = comprovanteService.createPdf(entrega);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("relatorio/mensal/generate/{mes}/{ano}")
    public ResponseEntity<?> generateRelatorioMensalPdf(@PathVariable String mes, @PathVariable String ano) {
        List<Entrega> entregas = entregaService.findAll();
        byte[] pdfBytes = entregaMensalService.createPdf(entregas, mes, ano);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorioMensal.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/comprovante")
    public ResponseEntity<Entrega> insert(@RequestBody EntregaDTO entregaDTO) {
        Entrega entrega = entregaService.insert(entregaDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entrega.getId()).toUri();
        return ResponseEntity.created(uri).body(entrega);
    }

    @PutMapping("/comprovante/{id}")
    public ResponseEntity<Entrega> update(@PathVariable Long id, @RequestBody Entrega obj) {
        Entrega entrega = entregaService.update(id, obj);
        return ResponseEntity.ok().body(entrega);
    }
}
