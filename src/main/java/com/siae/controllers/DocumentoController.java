package com.siae.controllers;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siae.entities.Documento;
import com.siae.services.DocumentoService;


@RestController
@RequestMapping("/document")
public class DocumentoController {
	
	private static final String FILE_DIRECTORY = "uploads/";
	
	@Autowired
	private DocumentoService service;
	
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
		service.delete(documentoId, produtorId);
		return ResponseEntity.noContent().build();
	}
}
