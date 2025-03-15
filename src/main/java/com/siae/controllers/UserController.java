package com.siae.controllers;

import java.net.URI;
import java.util.List;

import com.siae.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.siae.entities.User;
import com.siae.services.UserService;

@Controller
@RequestMapping("/public/users")
public class UserController {
	
	@Autowired UserService service;
	
	@GetMapping
	public ResponseEntity<List<User>> findAll() {
		List<User> users = service.findAll();
		return ResponseEntity.ok().body(users);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		User user = service.findById(id);
		return ResponseEntity.ok().body(user);
	}
	
	@PostMapping("/register")
	public ResponseEntity<User> insert(@RequestBody User obj) {
		User user = service.insert(obj);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).body(user);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<User> update(@PathVariable Long id, @RequestBody UserDTO request) {
		User user = request.getUser();
		System.out.println("USUÁRIO: " + user.toString());
		String role = request.getRole();
		System.out.println("USUÁRIO: " + role);
		user = service.update(id, user, role);
		return ResponseEntity.ok().body(user);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<User> delete(@PathVariable Long id){
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
	
}
