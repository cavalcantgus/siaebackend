package com.siae.repositories;

import com.siae.entities.NotificacaoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoUsuarioRepository extends JpaRepository<NotificacaoUsuario, Long> {
    List<NotificacaoUsuario> findByUsuarioId(Long id);
}
