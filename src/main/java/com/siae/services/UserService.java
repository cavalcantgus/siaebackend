package com.siae.services;

import java.util.List;
import java.util.Optional;

import com.siae.entities.ConfirmationToken;
import com.siae.exception.EmailAlreadyExists;
import com.siae.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.entities.Role;
import com.siae.entities.User;
import com.siae.enums.RoleName;
import com.siae.repositories.RoleRepository;
import com.siae.repositories.UserRepository;
import com.siae.security.CustomPasswordEncoder;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomPasswordEncoder encoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final UserRegistrationService userRegistrationService;

    @Autowired
    public UserService(UserRepository userRepository,
					   RoleRepository roleRepository,
                       CustomPasswordEncoder encoder,
					   ConfirmationTokenService confirmationTokenService,
					   UserRegistrationService userRegistrationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.confirmationTokenService = confirmationTokenService;
        this.userRegistrationService = userRegistrationService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findUsersByRole(RoleName roleName) {
        return userRepository.findByRoles_Name(roleName);
    }

    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public User findByCpf(String cpf) {
		if(cpf == null || cpf.isEmpty()) throw new IllegalArgumentException("Cpf não pode ser nulo");
        return userRepository.findByCpf(cpf).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public User insert(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExists("Email já cadastrado. Tente outro ou verifique sua caixa de entrada.");
        }

        User preparedUser = userRegistrationService.prepareUserForRegistration(user);
        User savedUser = userRepository.save(preparedUser);
        ConfirmationToken confirmationToken = confirmationTokenService.generateToken(savedUser);
        userRegistrationService.sendConfirmationEmail(savedUser, confirmationToken);
        return savedUser;
    }

    public User update(Long id, User newUserData, String role) {
        return userRepository.findById(id).map(existingUser -> {
            applyUserUpdates(existingUser, newUserData, role);
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    private void applyUserUpdates(User target, User source, String role) {
        updateUserRole(target, role);

        target.setUsername(source.getUsername());
        target.setEmail(source.getEmail());
        target.setCpf(source.getCpf());

        if (!source.getPassword().equals(target.getPassword())) {
            target.setPassword(encoder.passwordEnconder().encode(source.getPassword()));
        }
    }

    private void updateUserRole(User user, String role) {
        RoleName roleName = RoleName.valueOf(role.toUpperCase());
        Role newRole = roleRepository.findByName(roleName);
        if (newRole == null) throw new IllegalArgumentException("Role não encontrada");

        user.getRoles().clear();
        user.getRoles().add(newRole);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        userRepository.delete(user);
    }
} 
