package com.siae.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siae.entities.*;
import com.siae.relatorios.EntregaMensalProdutor;
import com.siae.services.PagamentoService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/pagamentos")
public class PagamentoController {

    private PagamentoService pagamentoService;
    private EntregaMensalProdutor entregaMensalProdutor;

    @Autowired
    public void PagamentoController(PagamentoService pagamentoService, EntregaMensalProdutor entregaMensalProdutor) {
        this.pagamentoService = pagamentoService;
        this.entregaMensalProdutor = entregaMensalProdutor;
    }

    @GetMapping
    public ResponseEntity<List<Pagamento>> findAll(){
        List<Pagamento> pagamentos = pagamentoService.findAll();
        return ResponseEntity.ok().body(pagamentos);
    }

    @GetMapping("/pagamento/{id}")
    public ResponseEntity<List<Pagamento>> findByProdutorId(@PathVariable Long id) {
        List<Pagamento> pagamentos = pagamentoService.findByProdutorId(id);
        return ResponseEntity.ok().body(pagamentos);
    }

    @GetMapping("relatorio/generate/{id}")
    public ResponseEntity<?> generateRelatorioMensalPdf(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.findById(id);
        byte[] pdfBytes = entregaMensalProdutor.createPdf(pagamento);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "comprovante.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
    @GetMapping("relatorio/visualize/{id}")
    public ResponseEntity<?> visualizeRelatorioMensalPdf(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.findById(id);
        byte[] pdfBytes = entregaMensalProdutor.createPdf(pagamento);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("comprovante.pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/pagamento")
    public ResponseEntity<List<Pagamento>> sendToPayment(@RequestBody List<Entrega> entregas){
        List<Pagamento> pagamentos = pagamentoService.sendToPayment(entregas);
        return ResponseEntity.ok().body(pagamentos);
    }

//    @PutMapping("/pagamento/{id}")
//    public ResponseEntity<Pagamento> update(@PathVariable Long id, @RequestBody Pagamento obj) {
//        MultipartFile nota = null;
//        Pagamento pagamento = pagamentoService.update(id, obj, nota);
//        return ResponseEntity.ok().body(pagamento);
//    }

    @PutMapping("/pagamento/upload/{id}")
    public ResponseEntity<Pagamento> upload(@PathVariable Long id,
                                            @RequestParam("pagamento") String pagamentoJson,
                                           @RequestParam(value = "file", required = false) MultipartFile notaFiscal) throws Exception {

        // Convertendo o JSON do produtor em um objeto Produtor
        Pagamento pagamento = new ObjectMapper().readValue(pagamentoJson, Pagamento.class);

        // Chamando o serviço para salvar o produtor e os documentos
        Pagamento obj = pagamentoService.update(id, pagamento, notaFiscal);

        // Retornando o produtor criado com o status HTTP 201
        return ResponseEntity.ok().body(obj);
    }

    @DeleteMapping("/pagamento/{id}")
    public ResponseEntity<Pagamento> delete(@PathVariable Long id) {
        pagamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
