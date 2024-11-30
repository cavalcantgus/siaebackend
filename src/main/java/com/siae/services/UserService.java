package com.siae.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siae.entities.User;
import com.siae.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

	@Autowired UserRepository repository;
	
	public List<User> findAll() {
		return repository.findAll();
	}
	
	public User findById(Long id) {
		Optional<User> user = repository.findById(id);
		return user.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
	}
	
	public User insert(User user) {
		return repository.save(user);
	}
	
	public User update(Long id, User user) {
		try {
			if(repository.existsById(id)) {
				User userTarget = repository.getReferenceById(id);
				updateData(user, userTarget);
				return repository.save(userTarget);
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
		userTarget.setPassword(user.getPassword());
	}
	
	public void delete(Long id) {
		try {
			if(repository.existsById(id)) {
				repository.deleteById(id);
			} else {
				throw new EntityNotFoundException("Usuário não encontrado");
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
} 
