package com.siae.services;

import com.siae.entities.ConfirmationToken;
import com.siae.entities.User;
import com.siae.repositories.ConfirmationTokenRepository;
import com.siae.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.UUID;

@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository, UserRepository userRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.userRepository = userRepository;
    }

    public ConfirmationToken getValidToken(String token) {
        return confirmationTokenRepository.findByToken(token)
                .filter(t -> t.getConfirmedAt() == null)
                .filter(t -> t.getExpiresAt().isAfter(ChronoLocalDate.from(LocalDateTime.now())))
                .orElseThrow(() -> new IllegalStateException("Token inválido ou expirado"));
    }

    public ConfirmationToken generateToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken(token);
        confirmationToken.setCreatedAt(LocalDate.now());
        confirmationToken.setExpiresAt(LocalDate.now().plusDays(1));
        confirmationToken.setToken(token);
        confirmationToken.setUser(user);

        return confirmationTokenRepository.save(confirmationToken);
    }

    public void confirmToken(ConfirmationToken token) {
        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        token.setConfirmedAt(LocalDate.now());
        confirmationTokenRepository.save(token);
    }
}
