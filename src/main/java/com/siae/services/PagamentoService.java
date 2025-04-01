package com.siae.services;

import com.siae.entities.*;
import com.siae.enums.StatusPagamento;
import com.siae.repositories.EntregaPagamentoRepository;
import com.siae.repositories.EntregaRepository;
import com.siae.repositories.PagamentoRepository;
import com.siae.repositories.ProdutorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PagamentoService {

    private final EntregaService entregaService;
    private ProdutorRepository produtorRepository;
    private PagamentoRepository pagamentoRepository;
    private EntregaPagamentoRepository entregaPagamentoRepository;

    @Autowired
    public PagamentoService(PagamentoRepository pagamentoRepository,
                            ProdutorRepository produtorRepository, EntregaPagamentoRepository entregaPagamentoRepository, EntregaService entregaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.produtorRepository = produtorRepository;
        this.entregaPagamentoRepository = entregaPagamentoRepository;
        this.entregaService = entregaService;
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

    public Pagamento update(Long id, Pagamento pagamento, String filePath) {
        try {
            if(pagamentoRepository.existsById(id)) {
                Pagamento pagamentoTarget = pagamentoRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Contrato não encontrado"));
                updateData(pagamento, pagamentoTarget, filePath);
                return pagamentoRepository.save(pagamentoTarget);
            } else {
                throw new EntityNotFoundException("Contrato não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateData(Pagamento pagamento, Pagamento pagamentoTarget, String filePath) {
        pagamentoTarget.setData(LocalDate.now());
        pagamentoTarget.setProdutor(pagamento.getProdutor());
        pagamentoTarget.setQuantidade(pagamento.getQuantidade());
        pagamentoTarget.setTotal(pagamento.getTotal());

        if(filePath != null) {
            pagamentoTarget.setNotaFiscal(filePath);
        }
        if(pagamento.getStatus() != null) {
            try {
                StatusPagamento statusPagamento = StatusPagamento.valueOf(pagamento.getStatus().name());
                pagamentoTarget.setStatus(statusPagamento);
            } catch (IllegalArgumentException e) {
                System.out.println("Status inválido: " + pagamento.getStatus());
            }
        }
    }
}
