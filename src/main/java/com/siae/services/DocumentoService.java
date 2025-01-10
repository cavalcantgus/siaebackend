package com.siae.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.siae.entities.Documento;
import com.siae.entities.Produtor;
import com.siae.repositories.DocumentoRepository;
import com.siae.repositories.ProdutorRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class DocumentoService {
	
	@Autowired
	private ProdutorRepository produtorRepository;
	
	@Autowired
	private DocumentoRepository repository;
	
	public Documento findById(Long id) {
		Optional<Documento> documento = repository.findById(id);
		return documento.orElseThrow(() -> new EntityNotFoundException("Erro"));
	}
	
	@Transactional
	public List<Documento> saveDocs(List<MultipartFile> documentos, Produtor produtor) {
		List<Documento> documentosEntidade = documentos.stream()
				.map(documento -> {
					try {
						String uploadDir = "uploads/";
						Path filePath = Paths.get(uploadDir + documento.getOriginalFilename());
						
						Files.createDirectories(filePath.getParent());
						Files.write(filePath, documento.getBytes());
						Documento doc = new Documento();
						doc.setFileName(documento.getOriginalFilename());
						doc.setFileType(documento.getContentType());
						doc.setFilePath(filePath.toString());
						doc.setProdutor(produtor);
						return doc;
					} catch (IOException e) {
		                throw new RuntimeException("Erro ao processar arquivo", e);
					}
				})
				.toList();
		
		repository.saveAll(documentosEntidade);
		return documentosEntidade;
	}
	
	public List<Documento> findByProdutor(Produtor produtor) {
		return repository.findByProdutor(produtor);
	}
	
	@Transactional
	public List<Documento> update(List<MultipartFile> documentos, Produtor produtor) {
		System.out.println("Recebendo documentos: " + documentos.size());
	    List<Documento> documentosExistentes = repository.findByProdutor(produtor);
	  
	    List<Documento> documentosAtualizados = documentosExistentes;

	    documentos.forEach(documento -> {
	        try {
	            // Diretório onde os arquivos serão armazenados
	            String uploadDir = "uploads/";
	            Path filePath = Paths.get(uploadDir + documento.getOriginalFilename());

	            Files.createDirectories(filePath.getParent());
	            Files.write(filePath, documento.getBytes());

	            // Verifique se o documento já existe
	            Documento documentoExistente = documentosExistentes.stream()
	                    .filter(doc -> doc.getFileName().equals(documento.getOriginalFilename()))
	                    .findFirst()
	                    .orElse(null);

	            if (documentoExistente != null) {
	                // Atualize os dados do documento existente
	                documentoExistente.setFileName(documento.getOriginalFilename());
	                documentoExistente.setFileType(documento.getContentType());
	                documentoExistente.setFilePath(filePath.toString());
	                documentosAtualizados.add(documentoExistente);
	            } else {
	                // Crie um novo documento e associe ao produtor
	                Documento doc = new Documento();
	                doc.setFileName(documento.getOriginalFilename());
	                doc.setFileType(documento.getContentType());
	                doc.setFilePath(filePath.toString());
	                doc.setProdutor(produtor);  // Associe diretamente ao produtor
	                documentosAtualizados.add(doc);
	            }
	        } catch (IOException e) {
	            throw new RuntimeException("Erro ao processar arquivo", e);
	        }
	    });

	    // Remover documentos que não foram enviados
	    List<Documento> documentosParaRemover = documentosExistentes.stream()
	            .filter(doc -> documentosAtualizados.stream()
	                    .noneMatch(docAtualizado -> docAtualizado.getFileName().equals(doc.getFileName())))
	            .toList();

	    documentosParaRemover.forEach(doc -> {
	        try {
	            // Exclua o arquivo fisicamente
	            Files.deleteIfExists(Paths.get(doc.getFilePath()));
	            // Exclua o documento do banco
	            repository.delete(doc);
	        } catch (IOException e) {
	            throw new RuntimeException("Erro ao remover arquivo antigo", e);
	        }
	    });

	    for(Documento documento : documentosAtualizados) {
	    	System.out.println(documento.getFileName());
	    }
	    
	    
	    List<Documento> persistidos = repository.saveAll(documentosAtualizados);
	    persistidos.forEach(doc -> System.out.println("Persistido: " + doc.getFileName() + ", Caminho: " + doc.getFilePath()));
	    return persistidos;
	}
	
	@Transactional
	public void delete(Long documentoId, Long produtorId) {
		Produtor produtor = produtorRepository.findById(produtorId).orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
		Documento documento = repository.findById(documentoId).orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));
		if (!produtor.getDocumentos().contains(documento)) {
		    throw new IllegalArgumentException("Documento não pertence ao produtor informado");
		}
		produtor.getDocumentos().remove(documento);
		
		produtorRepository.save(produtor);
		
		repository.deleteById(documentoId);
	}

}
