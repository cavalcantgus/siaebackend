package com.siae.services;

import com.siae.entities.Entrega;
import com.siae.entities.EntregaPagamento;
import com.siae.entities.Pagamento;
import com.siae.repositories.EntregaPagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntregaPagamentoService {

    private final EntregaPagamentoRepository entregaPagamentoRepository;

    @Autowired
    public EntregaPagamentoService(EntregaPagamentoRepository entregaPagamentoRepository) {
        this.entregaPagamentoRepository = entregaPagamentoRepository;
    }

    public EntregaPagamento insert(EntregaPagamento entregaPagamento) {
        return entregaPagamentoRepository.save(entregaPagamento);
    }

    public void mountEntregaPagamento(List<Entrega> entregas, Pagamento pagamento) {
        for (Entrega entrega : entregas) {
            EntregaPagamento entregaPagamento = new EntregaPagamento();
            entregaPagamento.setEntrega(entrega);
            entregaPagamento.setPagamento(pagamento);
            insert(entregaPagamento);
        }
    }
}
