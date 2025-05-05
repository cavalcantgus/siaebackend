package com.siae.controllers;

import com.siae.services.NotaFiscalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/public/notas")
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;
    private static final String FILE_DIRECTORY = "uploads/";

    @Autowired
    public NotaFiscalController(NotaFiscalService notaFiscalService) {
        this.notaFiscalService = notaFiscalService;
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String fileName) {
        Path path = Paths.get(FILE_DIRECTORY + fileName);
        Resource resource = new FileSystemResource(path);

        if(!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"");

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @DeleteMapping("/delete/{documentoId}/{produtorId}")
    public ResponseEntity<Void> delete(@PathVariable Long documentoId, @PathVariable Long produtorId) {
        notaFiscalService.delete(documentoId, produtorId);
        return ResponseEntity.noContent().build();
    }
}
