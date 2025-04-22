package com.siae.services;

import java.util.List;
import java.util.Optional;

import com.siae.mappers.ProductorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.siae.entities.Documento;
import com.siae.entities.Produtor;
import com.siae.repositories.ProdutorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutorService {

    private final ProdutorRepository produtorRepository;
    private final DocumentoService documentoService;
    private final ProductorMapper produtorMapper;

    @Autowired
    public ProdutorService(ProdutorRepository produtorRepository,
                           DocumentoService documentoService,
                           ProductorMapper produtorMapper) {
        this.produtorRepository = produtorRepository;
        this.documentoService = documentoService;
        this.produtorMapper = produtorMapper;
    }

    public List<Produtor> findAll() {
        return produtorRepository.findAll();
    }

    public List<Documento> findAllDocumentoProdutor(Produtor produtor) {
        return documentoService.findByProdutor(produtor);
    }

    public Produtor findById(Long id) {
        Optional<Produtor> produtor = produtorRepository.findById(id);
        return produtor.orElseThrow(() -> new EntityNotFoundException("Erro"));
    }

    public Produtor insert(Produtor produtor, List<MultipartFile> documentos) {
        Produtor savedProdutor = produtorRepository.save(produtor);
        if (documentos != null) {
            List<Documento> documentosProdutor = documentoService.saveDocs(documentos, produtor);
            for (Documento documento : documentosProdutor) {
                savedProdutor.getDocumentos().add(documento);
            }
        }

        return savedProdutor;
    }

    
    public Produtor update(Long id, Produtor produtor, List<MultipartFile> documentos) {
        if (!produtorRepository.existsById(id)) {
            throw new EntityNotFoundException("Produtor não encontrado");
        }

        Produtor produtorTarget = produtorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));

        produtorMapper.updateProductor(produtorTarget, produtor);

        if (documentos != null && !documentos.isEmpty()) {
            List<Documento> documentosProdutorAtualizados = documentoService.update(documentos, produtorTarget);
            produtorTarget.getDocumentos().clear();
            for (Documento documento : documentosProdutorAtualizados) {
                System.out.println("Documento: " + documento);
                produtorTarget.getDocumentos().add(documento);
            }
        }

        return produtorRepository.save(produtorTarget);

    }
}
