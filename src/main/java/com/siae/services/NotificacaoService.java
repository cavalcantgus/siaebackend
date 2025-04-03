package com.siae.services;

import com.siae.entities.Notificacao;
import com.siae.entities.NotificacaoUsuario;
import com.siae.entities.Role;
import com.siae.entities.User;
import com.siae.enums.RoleName;
import com.siae.repositories.NotificacaoRepository;
import com.siae.repositories.NotificacaoUsuarioRepository;
import com.siae.repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final NotificacaoUsuarioRepository notificacaoUsuarioRepository;
    private NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UserService userService, RoleRepository roleRepository, NotificacaoUsuarioRepository notificacaoUsuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.notificacaoUsuarioRepository = notificacaoUsuarioRepository;
    }

    public List<Notificacao> findAll() {
        return notificacaoRepository.findAll();
    }

    public Notificacao findById(Long id) {
        Optional<Notificacao> obj = notificacaoRepository.findById(id);
        return obj.orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada!"));
    }

    public List<Notificacao> buscarNotificacoesPorUsuario(Long userId) {
        List<NotificacaoUsuario> notificacaoUsuarios = notificacaoUsuarioRepository.findByUsuarioId(userId);

        return notificacaoUsuarios.stream()
                .map(NotificacaoUsuario::getNotificacao)
                .collect(Collectors.toList());
    }

    public void enviarNotificacaoParaRole(String titulo, String mensagem, RoleName roleName) {
        Role role = roleRepository.findByName(roleName);

        Notificacao notificacao = new Notificacao();
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setData(LocalDate.now());
        notificacao.setRole(role);
        notificacaoRepository.save(notificacao);

        List<User> usuarios = userService.findUsersByRole(roleName);

        for(User user : usuarios){
            NotificacaoUsuario notificacaoUsuario = new NotificacaoUsuario();
            notificacaoUsuario.setUsuario(user);
            notificacaoUsuario.setNotificacao(notificacao);
            notificacaoUsuario.setData(LocalDate.now());
            notificacaoUsuarioRepository.save(notificacaoUsuario);
        }
    }
}
