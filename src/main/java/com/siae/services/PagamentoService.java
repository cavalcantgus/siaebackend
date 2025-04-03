package com.siae.services;

import com.siae.entities.*;
import com.siae.enums.StatusPagamento;
import com.siae.repositories.EntregaPagamentoRepository;
import com.siae.repositories.NotaFiscalRepository;
import com.siae.repositories.PagamentoRepository;
import com.siae.repositories.ProdutorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PagamentoService {

    private final EntregaService entregaService;
    private final DocumentoService documentoService;
    private final NotaFiscalService notaFiscalService;
    private final NotaFiscalRepository notaFiscalRepository;
    private ProdutorRepository produtorRepository;
    private PagamentoRepository pagamentoRepository;
    private EntregaPagamentoRepository entregaPagamentoRepository;

    @Autowired
    public PagamentoService(PagamentoRepository pagamentoRepository,
                            ProdutorRepository produtorRepository, EntregaPagamentoRepository entregaPagamentoRepository, EntregaService entregaService, DocumentoService documentoService, NotaFiscalService notaFiscalService, NotaFiscalRepository notaFiscalRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.produtorRepository = produtorRepository;
        this.entregaPagamentoRepository = entregaPagamentoRepository;
        this.entregaService = entregaService;
        this.documentoService = documentoService;
        this.notaFiscalService = notaFiscalService;
        this.notaFiscalRepository = notaFiscalRepository;
    }

    public List<Pagamento> findAll() {
        return pagamentoRepository.findAll();
    }

    public Pagamento findById(Long id) {
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id);
        return pagamento.orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
    }

    @Transactional
    public List<Pagamento> sendToPayment(List<Entrega> entregas) {
        if (entregas.isEmpty()) {
            throw new IllegalArgumentException("Lista de entregas vazia");
        }

        List<EntregaPagamento> entregaPagamentos = new ArrayList<>();
        Map<String, Pagamento> pagamentosMap = new HashMap<>();

        for (Entrega entrega : entregas) {
            Entrega entregaGerenciada = entregaService.findById(entrega.getId());
            entregaGerenciada.setEnviadoParaPagamento(true);
            Produtor produtor = entrega.getProdutor();
            int ano = entrega.getDataDaEntrega().getYear();
            int mes = entrega.getDataDaEntrega().getMonthValue();

            // Criamos uma chave única para identificar um pagamento (Produtor + Ano + Mês)
            String key = produtor.getId() + "-" + ano + "-" + mes;

            Pagamento pagamento = pagamentosMap.get(key);

            if (pagamento == null) {
                // Busca no banco apenas se ainda não estiver no mapa
                pagamento = pagamentoRepository.findByProdutorAndAnoAndMes(produtor, ano, mes);

                if (pagamento == null) {
                    // Se não existir, cria um novo
                    System.out.println("Não existe");
                    pagamento = new Pagamento();

                    pagamento.setProdutor(produtor);
                    pagamento.setQuantidade(BigDecimal.ZERO);
                    pagamento.setTotal(BigDecimal.ZERO);
                    pagamento.setData(LocalDate.now());
                    pagamento.setStatus(StatusPagamento.AGUARDANDO_NF);
                }

                pagamentosMap.put(key, pagamento);
            }

            // Atualiza os valores do pagamento
            pagamento.setQuantidade(pagamento.getQuantidade().add(entrega.getQuantidade()));
            pagamento.setTotal(pagamento.getTotal().add(entrega.getTotal()));

            // Cria a relação EntregaPagamento
            EntregaPagamento entregaPagamento = new EntregaPagamento();
            entregaPagamento.setPagamento(pagamento);
            entregaPagamento.setEntrega(entrega);
            entregaPagamentos.add(entregaPagamento);
        }

        // Salva as relações na tabela intermediária
        entregaPagamentoRepository.saveAll(entregaPagamentos);

        // Salva os pagamentos atualizados
        return pagamentoRepository.saveAll(pagamentosMap.values());
    }

    public Pagamento update(Long id, Pagamento pagamento, MultipartFile notaFiscal) {
        try {
            if (pagamentoRepository.existsById(id)) {
                Pagamento pagamentoTarget = pagamentoRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Contrato não encontrado"));
                updateData(pagamento, pagamentoTarget, notaFiscal);
                return pagamentoRepository.save(pagamentoTarget);
            } else {
                throw new EntityNotFoundException("Contrato não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateData(Pagamento pagamento, Pagamento pagamentoTarget, MultipartFile notaFiscal) {
        pagamentoTarget.setData(LocalDate.now());
        pagamentoTarget.setProdutor(pagamento.getProdutor());
        pagamentoTarget.setQuantidade(pagamento.getQuantidade());
        pagamentoTarget.setTotal(pagamento.getTotal());

        if(notaFiscal != null) {
            NotaFiscal nota;
            NotaFiscal notaFiscalExistente = notaFiscalRepository.findByPagamentoId(pagamentoTarget.getId());

            if (notaFiscalExistente != null) {
                // Atualiza a nota fiscal existente
                nota = notaFiscalService.update(notaFiscal, pagamentoTarget);
            } else {
                // Insere uma nova nota fiscal
                nota = notaFiscalService.insert(notaFiscal, pagamentoTarget);
            }
            // Certifique-se de definir a nota fiscal no pagamentoTarget
            pagamentoTarget.setNotaFiscal(nota);
        }
        
        if (pagamento.getStatus() != null) {
            try {
                StatusPagamento statusPagamento = StatusPagamento.valueOf(pagamento.getStatus().name());
                pagamentoTarget.setStatus(statusPagamento);
            } catch (IllegalArgumentException e) {
                System.out.println("Status inválido: " + pagamento.getStatus());
            }
        }
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            if (pagamentoRepository.existsById(id)) {
                List<EntregaPagamento> entregasPagamentos = entregaPagamentoRepository.findByPagamentoId(id);

                for (EntregaPagamento entregaPagamento : entregasPagamentos) {
                    entregaPagamento.getEntrega().setEnviadoParaPagamento(false);
                }

                entregaPagamentoRepository.deleteAll(entregasPagamentos);

                pagamentoRepository.deleteById(id);
            } else {
                throw new EntityNotFoundException("Pagamento não encontrado");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
