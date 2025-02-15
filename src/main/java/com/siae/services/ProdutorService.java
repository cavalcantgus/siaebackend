package com.siae.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.siae.entities.Documento;
import com.siae.entities.Produtor;
import com.siae.repositories.ProdutorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutorService {
	
	@Autowired
	private ProdutorRepository produtorRepository;
	
	@Autowired
	private DocumentoService documentoService;
	
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
		if(documentos != null) {
			List<Documento> documentosProdutor = documentoService.saveDocs(documentos, produtor);
			for(Documento documento : documentosProdutor) {
				savedProdutor.getDocumentos().add(documento);
			}
		}
		
		return savedProdutor;
	}
	
	public Produtor update(Long id, Produtor produtor, List<MultipartFile> documentos) {
	    try {
	        if (produtorRepository.existsById(id)) {
	            Produtor produtorTarget = produtorRepository.findById(id)
	                    .orElseThrow(() -> new EntityNotFoundException("Produtor não encontrado"));

	            updateData(produtor, produtorTarget);
	            
	            if (documentos != null && !documentos.isEmpty()) {
	    	        List<Documento> documentosProdutorAtualizados = documentoService.update(documentos, produtorTarget);
	    	        produtorTarget.getDocumentos().clear();
	    	        for(Documento documento: documentosProdutorAtualizados) {
	    	        	System.out.println("Documento: " + documento);
		    	        produtorTarget.getDocumentos().add(documento);
	    	        }
	    	    }


	            return produtorRepository.save(produtorTarget);
	        } else {
	            throw new EntityNotFoundException("Produtor não encontrado");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();  
	        return null;
	    }
	}


	private void updateData(Produtor produtor, Produtor produtorTarget) {
		produtorTarget.setNome(produtor.getNome());
		produtorTarget.setEmail(produtor.getEmail());
		produtorTarget.setDataNascimento(produtor.getDataNascimento());
		produtorTarget.setEndereco(produtor.getEndereco());
		produtorTarget.setCpf(produtor.getCpf());
		produtorTarget.setRg(produtor.getRg());
		produtorTarget.setCep(produtor.getCep());
		produtorTarget.setContato(produtor.getContato());
		produtorTarget.setMunicipio(produtor.getMunicipio());
		produtorTarget.setEstado(produtor.getEstado());
		produtorTarget.setBanco(produtor.getBanco());
		produtorTarget.setAgencia(produtor.getAgencia());
		produtorTarget.setConta(produtor.getConta());
		produtorTarget.setCaf(produtor.getCaf());
		produtorTarget.setValidadeCaf(produtor.getValidadeCaf());
		produtorTarget.setTipoConta(produtor.getTipoConta());
		produtorTarget.setEscolaridade(produtor.getEscolaridade());
		produtorTarget.setEstadoCivil(produtor.getEstadoCivil());
		produtorTarget.setSexo(produtor.getSexo());
	}
	
	public void deleteById(Long id) {
		try {
			if(produtorRepository.existsById(id)) {
				produtorRepository.deleteById(id);
			} else {
				throw new EntityNotFoundException("Produtor não encontrado");
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}
}
