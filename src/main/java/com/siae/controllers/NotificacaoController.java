package com.siae.controllers;

import com.siae.entities.Notificacao;
import com.siae.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @Autowired
    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Notificacao>> buscarNotificacaoPorUsuario(@PathVariable long id) {
        List<Notificacao> notificacoes = notificacaoService.buscarNotificacoesPorUsuario(id);
        return ResponseEntity.ok().body(notificacoes);
    }
}
