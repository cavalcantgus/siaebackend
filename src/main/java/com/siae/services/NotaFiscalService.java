package com.siae.services;

import com.siae.entities.NotaFiscal;
import com.siae.entities.Pagamento;
import com.siae.repositories.NotaFiscalRepository;
import com.siae.repositories.PagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class NotaFiscalService {

    private final NotaFiscalRepository notaFiscalRepository;
    private final PagamentoRepository pagamentoRepository;

    @Autowired
    public NotaFiscalService(NotaFiscalRepository repo, PagamentoRepository pagamentoRepository) {
        this.notaFiscalRepository = repo;
        this.pagamentoRepository = pagamentoRepository;
    }

    public List<NotaFiscal> findAll() {
        return notaFiscalRepository.findAll();
    }

    public NotaFiscal findById(Long id) {
        Optional<NotaFiscal> obj = notaFiscalRepository.findById(id);
        return obj.orElseThrow(() -> new EntityNotFoundException("Nota Fiscal não encontrada"));
    }

    public NotaFiscal insert(MultipartFile obj, Pagamento pagamento) {
        NotaFiscal nota = new NotaFiscal();
        try {
            String uploadDir = "uploads/";
            Path filePath = Paths.get(uploadDir + obj.getOriginalFilename());

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, obj.getBytes());
            nota.setFileName(obj.getOriginalFilename());
            nota.setFileType(obj.getContentType());
            nota.setFilePath(filePath.toString());
            nota.setPagamento(pagamento);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar arquivo", e);
        }
        notaFiscalRepository.save(nota);
        return nota;

    }

    @Transactional
    public void delete(Long notaId, Long pagamentoId) {
        Pagamento pagamento =
                pagamentoRepository.findById(pagamentoId).orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
        NotaFiscal notaFiscal = notaFiscalRepository.findById(notaId).orElseThrow(() -> new EntityNotFoundException("Nota não encontrada"));
        if (!pagamento.getNotaFiscal().equals(notaFiscal)) {
            throw new IllegalArgumentException("Nota Fiscal não pertence ao pagamento informado");
        }
        pagamento.setNotaFiscal(null);

        pagamentoRepository.save(pagamento);

        notaFiscalRepository.deleteById(notaId);
    }
}
