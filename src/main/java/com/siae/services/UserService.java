package com.siae.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.siae.entities.ConfirmationToken;
import com.siae.exception.EmailAlreadyExists;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
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

	@Autowired UserRepository userRepository;
	
	@Autowired RoleRepository roleRepository;
	
	@Autowired
	private CustomPasswordEncoder encoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenService verificationTokenService;
    @Autowired
    private ConfirmationTokenService confirmationTokenService;

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
		return userRepository.findByCpf(cpf);
	}

	@Transactional
	public void enableUser(User user) {
		user.setEnabled(true);
		userRepository.save(user);
	}
	
	public User insert(User user) {
		if(userRepository.existsByEmail(user.getEmail())) {
			throw new EmailAlreadyExists("Email já cadastrado. Tente outro ou verifique sua caixa de entrada.");
		}
		String roleNameInput = "PENDENTE";
		RoleName roleName = RoleName.valueOf(roleNameInput); 
		Role pendingRole = roleRepository.findByName(roleName);
		user.setRoles(Set.of(pendingRole));

		user.setPassword(encoder.passwordEnconder().encode(user.getPassword()));
		User savedUser = userRepository.save(user);

		ConfirmationToken confirmationToken = confirmationTokenService.generateToken(user);

		String link = null;
        try {
            link = verificationTokenService.buildVerificationLink(confirmationToken.getToken(),
					user.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            emailService.sendEmail(user.getEmail(), "Confirmação de Email", link);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return savedUser;
	}

	public User update(Long id, User user, String role) {
		try {
			if (!userRepository.existsById(id)) {
				throw new EntityNotFoundException("Usuário não encontrado");
			}

			User userTarget = userRepository.getReferenceById(id);
			updateData(user, userTarget, role);
			return userRepository.save(userTarget);

		} catch (Exception e) {
			e.printStackTrace(); // Para debugar o erro corretamente
			return null;
		}
	}

	private void updateData(User user, User userTarget, String role) {
		System.out.println("String Role: " + role);

		try {
			RoleName roleName = RoleName.valueOf(role.toUpperCase());
			System.out.println("ROLENAME: " + roleName);

			// Obtém a Role atual do usuário
			Role oldRole = userTarget.getRoles().iterator().next();
			System.out.println("ROLE: " + oldRole);

			// Remove a Role antiga corretamente
			userTarget.getRoles().remove(oldRole);

			// Busca a nova Role
			Role newRole = roleRepository.findByName(roleName);
			if (newRole != null) {
				userTarget.getRoles().add(newRole);
			} else {
				throw new IllegalArgumentException("Role não encontrada: " + role);
			}

			// Atualiza os dados do usuário
			userTarget.setUsername(user.getUsername());
			userTarget.setEmail(user.getEmail());

			// Atualiza senha apenas se for diferente da atual
			if (!user.getPassword().equals(userTarget.getPassword())) {
				userTarget.setPassword(encoder.passwordEnconder().encode(user.getPassword()));
			}

		} catch (IllegalArgumentException e) {
			System.err.println("Erro ao converter Role: " + e.getMessage());
		}
	}

	public void delete(Long id) {
		try {
			if(userRepository.existsById(id)) {
				userRepository.deleteById(id);
			} else {
				throw new EntityNotFoundException("Usuário não encontrado");
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
} 
