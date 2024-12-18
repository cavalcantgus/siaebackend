package com.siae.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siae.entities.Produtor;
import com.siae.services.ProdutorService;

@RestController
@RequestMapping("/public/produtores")
public class ProdutorController {

    @Autowired
    private ProdutorService service;
    
    @GetMapping
    public ResponseEntity<List<Produtor>> findAll() {
    	List<Produtor> produtores = service.findAll();
    	return ResponseEntity.ok().body(produtores);
    }

    @PostMapping("/produtor")
    public ResponseEntity<Produtor> insert(@RequestParam("produtor") String produtorJson, 
                                           @RequestParam(value = "file", required = false) List<MultipartFile> documentos) throws Exception {
        
        // Convertendo o JSON do produtor em um objeto Produtor
        Produtor produtor = new ObjectMapper().readValue(produtorJson, Produtor.class);
        
        // Chamando o serviço para salvar o produtor e os documentos
        Produtor obj = service.insert(produtor, documentos);
        
        // Construindo a URI para o novo produtor
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                                             .path("/{id}")
                                             .buildAndExpand(obj.getId())
                                             .toUri();
        
        // Retornando o produtor criado com o status HTTP 201
        return ResponseEntity.created(uri).body(obj);
    }
    
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Produtor> update(@PathVariable Long id,
                                           @RequestParam("produtor") String produtorJson,
                                           @RequestParam(value = "file", required = false) List<MultipartFile> documentos)
            throws JsonMappingException, JsonProcessingException {

        // Converta o JSON para o objeto Produtor
        Produtor produtor = new ObjectMapper().readValue(produtorJson, Produtor.class);

        // Atualize o produtor com os documentos
        Produtor obj = service.update(id, produtor, documentos);

        // Retorne a resposta com o produtor atualizado
        return ResponseEntity.ok().body(obj);
    }

}
