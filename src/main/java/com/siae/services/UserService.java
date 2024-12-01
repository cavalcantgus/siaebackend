package com.siae.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	
	public User insert(User user) {
		String roleNameInput = "PENDENTE";
		RoleName roleName = RoleName.valueOf(roleNameInput); 
		Role pendingRole = roleRepository.findByName(roleName);
		user.setRoles(Set.of(pendingRole));
		user.setPassword(encoder.passwordEnconder().encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	public User update(Long id, User user) {
		try {
			if(userRepository.existsById(id)) {
				User userTarget = userRepository.getReferenceById(id);
				updateData(user, userTarget);
				return userRepository.save(userTarget);
			} else {
				throw new EntityNotFoundException("Usuário não encontrado");
			}
		} catch (Exception e) {
			e.getStackTrace();
			return null;
		}
	}

	private void updateData(User user, User userTarget) {
		userTarget.setUsername(user.getUsername());
		userTarget.setEmail(user.getEmail());
		userTarget.setPassword(encoder.passwordEnconder().encode(user.getPassword()));
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
