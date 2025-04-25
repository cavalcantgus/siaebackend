package com.siae.services;

import com.siae.entities.ConfirmationToken;
import com.siae.entities.Role;
import com.siae.entities.User;
import com.siae.enums.RoleName;
import com.siae.repositories.RoleRepository;
import com.siae.security.CustomPasswordEncoder;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

@Service
public class UserRegistrationService {

    private final CustomPasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    @Autowired
    public UserRegistrationService(CustomPasswordEncoder encoder
            , RoleRepository roleRepository
            , VerificationTokenService verificationTokenService
            , EmailService emailService) {
        this.encoder = encoder;
        this.roleRepository = roleRepository;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
    }

    public User prepareUserForRegistration(User user) {
        String roleNameInput = "PENDENTE";
        RoleName roleName = RoleName.valueOf(roleNameInput);
        Role pendingRole = roleRepository.findByName(roleName);
        user.setRoles(Set.of(pendingRole));
        user.setPassword(encoder.passwordEnconder().encode(user.getPassword()));
        user.setDataDeCadastro(LocalDate.now());
        return user;
    }

    public void sendConfirmationEmail(User user, ConfirmationToken confirmationToken) {
        try {
            String link = verificationTokenService.buildVerificationLink(confirmationToken.getToken(), user.getUsername());
            emailService.sendEmail(user.getEmail(), "Confirmação de Email", link);
        } catch (IOException | MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de confirmação: " + e);
        }
    }
}
