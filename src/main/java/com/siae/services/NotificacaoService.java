package com.siae.services;

import com.siae.entities.*;
import com.siae.enums.RoleName;
import com.siae.messaging.RabbitMQProducer;
import com.siae.repositories.NotificacaoRepository;
import com.siae.repositories.NotificacaoUsuarioRepository;
import com.siae.repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacaoService {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final NotificacaoUsuarioRepository notificacaoUsuarioRepository;
    private final ProdutorService produtorService;
    private final RabbitMQProducer rabbitMQProducer;
    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UserService userService, RoleRepository roleRepository, NotificacaoUsuarioRepository notificacaoUsuarioRepository, ProdutorService produtorService, RabbitMQProducer rabbitMQProducer) {
        this.notificacaoRepository = notificacaoRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.notificacaoUsuarioRepository = notificacaoUsuarioRepository;
        this.produtorService = produtorService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public List<Notificacao> findAll() {
        return notificacaoRepository.findAll();
    }

    public Notificacao findById(Long id) {
        Optional<Notificacao> obj = notificacaoRepository.findById(id);
        return obj.orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada!"));
    }

    public List<Notificacao> buscarNotificacoesPorUsuario(Long userId) {
        System.out.println("User ID: " + userId);
        List<NotificacaoUsuario> notificacaoUsuarios = notificacaoUsuarioRepository.findByUsuarioId(userId);
        System.out.println("NotificacaoUsuarios: " + notificacaoUsuarios);

        List<Notificacao> notificacoes = new ArrayList<>();

        for (NotificacaoUsuario notificacaoUsuario : notificacaoUsuarios) {
            try {
                Notificacao notificacao = notificacaoUsuario.getNotificacao();
                if (notificacao != null) {
                    notificacoes.add(notificacao);
                } else {
                    // Log caso o usuário não tenha notificações associadas corretamente
                    System.out.println("Notificação não encontrada para o usuário com ID: " + userId);
                }
            } catch (EntityNotFoundException e) {
                // Tratar a exceção quando a notificação não for encontrada
                System.out.println("Erro ao buscar a Notificação: " + e.getMessage());
                // Log da exceção para diagnóstico posterior
                e.printStackTrace();
            }
        }

        return notificacoes;
    }


    public void enviarNotificacaoParaRole(String titulo, String mensagem, RoleName roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role == null) return;

        Notificacao notificacao = new Notificacao();
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setData(LocalDate.now());
        notificacao.setRole(role);
        notificacaoRepository.save(notificacao);

        List<User> usuarios = userService.findUsersByRole(roleName);
        if (usuarios == null || usuarios.isEmpty()) return;

        for (User user : usuarios) {
            NotificacaoUsuario notificacaoUsuario = new NotificacaoUsuario();
            notificacaoUsuario.setUsuario(user);
            notificacaoUsuario.setNotificacao(notificacao);
            notificacaoUsuario.setData(LocalDate.now());
            notificacaoUsuarioRepository.save(notificacaoUsuario);
        }
    }


    @Transactional
    public void enviarNotificacaoParaUsuario(Long produtorId, int size) {
        Produtor produtor = produtorService.findById(produtorId);
        Optional<User> optionalUser = userService.findByCpf(produtor.getCpf());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String titulo = size + (size > 1 ? " entregas enviadas para o pagamento" : " entrega enviada para o pagamento");

            Notificacao notificacao = new Notificacao();
            notificacao.setTitulo(titulo);
            notificacao.setMensagem("Você tem " + titulo + " e aguardando efetuação");
            notificacao.setData(LocalDate.now());
            notificacaoRepository.save(notificacao);

            NotificacaoUsuario notificacaoUsuario = new NotificacaoUsuario();
            notificacaoUsuario.setUsuario(user);
            notificacaoUsuario.setNotificacao(notificacao);
            notificacaoUsuario.setData(LocalDate.now());
            notificacaoUsuarioRepository.save(notificacaoUsuario);

            rabbitMQProducer.sendMessage("Você tem novas entregas enviadas para o pagamento",
                    user.getEmail(), "Entregas enviadas para pagamento", user.getUsername(),
                    "notification-entrega.html");
        }

    }


    public void notificacaoPagamentoEfetuado(Produtor produtor) {
        Optional<User> optionalUser = userService.findByCpf(produtor.getCpf());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            Notificacao notificacao = new Notificacao();
            notificacao.setTitulo("Pagamento efetuado");
            notificacao.setMensagem("Você tem pagamento com status EFETUADO");
            notificacao.setData(LocalDate.now());
            notificacaoRepository.save(notificacao);

            NotificacaoUsuario notificacaoUsuario = new NotificacaoUsuario();
            notificacaoUsuario.setUsuario(user);
            notificacaoUsuario.setNotificacao(notificacao);
            notificacaoUsuario.setData(LocalDate.now());
            notificacaoUsuarioRepository.save(notificacaoUsuario);

            rabbitMQProducer.sendMessage("Você tem pagamento com status EFETUADO",
                    user.getEmail(), "Entregas enviadas para pagamento", user.getUsername(),
                    "pagamento-efetuado.html");
        }
    }
}
