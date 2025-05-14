package com.siae.services;

import com.siae.entities.*;
import com.siae.enums.RoleName;
import com.siae.enums.StatusPagamento;
import com.siae.exception.ResourceNotFoundException;
import com.siae.repositories.EntregaPagamentoRepository;
import com.siae.repositories.NotaFiscalRepository;
import com.siae.repositories.PagamentoRepository;
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
    private final NotaFiscalService notaFiscalService;
    private final NotaFiscalRepository notaFiscalRepository;
    private final NotificacaoService notificacaoService;
    private final EntregaPagamentoService entregaPagamentoService;
    private final PagamentoRepository pagamentoRepository;
    private final EntregaPagamentoRepository entregaPagamentoRepository;

    @Autowired
    public PagamentoService(PagamentoRepository pagamentoRepository,
                            EntregaPagamentoRepository entregaPagamentoRepository,
                            EntregaService entregaService,
                            NotaFiscalService notaFiscalService,
                            NotaFiscalRepository notaFiscalRepository,
                            NotificacaoService notificacaoService,
                            EntregaPagamentoService entregaPagamentoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.entregaPagamentoRepository = entregaPagamentoRepository;
        this.entregaService = entregaService;
        this.notaFiscalService = notaFiscalService;
        this.notaFiscalRepository = notaFiscalRepository;
        this.notificacaoService = notificacaoService;
        this.entregaPagamentoService = entregaPagamentoService;
    }

    public List<Pagamento> findAll() {
        return pagamentoRepository.findAll();
    }

    public Pagamento findById(Long id) {
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id);
        return pagamento.orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
    }

    public List<Pagamento> findByProdutorId(Long produtorId) {
        return pagamentoRepository.findByProdutorId(produtorId);
    }

    @Transactional
    public List<Pagamento> sendToPayment(List<Entrega> entregas, String mesReferente) {
        if (entregas.isEmpty()) {
            throw new IllegalArgumentException("Lista de entregas vazia");
        }

        Map<Long, Pagamento> pagamentosMap = new HashMap<>();
        Map<Pagamento, List<Entrega>> entregasPorPagamento = new HashMap<>();

        for (Entrega entrega : entregas) {
            Entrega entregaGerenciada = entregaService.findById(entrega.getId());
            entregaGerenciada.setEnviadoParaPagamento(true);
            Produtor produtor = entrega.getProdutor();
            Long produtorId = produtor.getId();

            Pagamento pagamento = pagamentosMap.get(produtorId);

            if (pagamento == null) {
                pagamento = new Pagamento();
                pagamento.setProdutor(produtor);
                pagamento.setQuantidade(BigDecimal.ZERO);
                pagamento.setTotal(BigDecimal.ZERO);
                pagamento.setData(LocalDate.now());
                pagamento.setMesReferente(mesReferente);
                pagamento.setStatus(StatusPagamento.AGUARDANDO_NF);
                pagamentosMap.put(produtorId, pagamento);
            }

            pagamento.setQuantidade(pagamento.getQuantidade().add(entrega.getQuantidade()));
            pagamento.setTotal(pagamento.getTotal().add(entrega.getTotal()));

            entregasPorPagamento.computeIfAbsent(pagamento, p -> new ArrayList<>()).add(entrega);
        }

        List<Pagamento> savedPayments = pagamentoRepository.saveAll(entregasPorPagamento.keySet());

        for(Pagamento pagamento : savedPayments) {
            List<Entrega> entregasRelacionadas = entregasPorPagamento.get(pagamento);
            entregaPagamentoService.mountEntregaPagamento(entregasRelacionadas, pagamento);
            notificacaoService.enviarNotificacaoParaUsuario(pagamento.getProdutor().getId(),
                    entregasRelacionadas.size());

        }

        notificacaoService.enviarNotificacaoParaRole("Novos Pagamentos Disponíveis",
                "Entregas foram enviadas para o pagamento.", RoleName.PAGAMENTO);

        return savedPayments;
    }

    @Transactional
    public Pagamento update(Long id, Pagamento pagamento, MultipartFile notaFiscal) {
        Pagamento pagamentoTarget = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", id));
        updateData(pagamento, pagamentoTarget, notaFiscal);

        if (pagamentoTarget.getStatus().equals(StatusPagamento.EFETUADO)) {
            notificacaoService.notificacaoPagamentoEfetuado(pagamento.getProdutor());
        }
        return pagamentoRepository.save(pagamentoTarget);

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
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", id));
        List<EntregaPagamento> entregaPagamentos = entregaPagamentoRepository.findByPagamentoId(id);
        for(EntregaPagamento entregaPagamento : entregaPagamentos) {
            entregaPagamento.getEntrega().setEnviadoParaPagamento(false);
        }
        entregaPagamentoRepository.deleteAll(entregaPagamentos);
        pagamentoRepository.delete(pagamento);
    }
}
