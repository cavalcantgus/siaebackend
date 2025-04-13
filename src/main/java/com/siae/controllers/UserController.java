package com.siae.controllers;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.siae.dto.UserDTO;
import com.siae.entities.ConfirmationToken;
import com.siae.exception.EmailAlreadyExists;
import com.siae.services.ConfirmationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.siae.entities.User;
import com.siae.services.UserService;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/public/users")
public class UserController {
	
	@Autowired UserService service;
    @Autowired
    private ConfirmationTokenService confirmationTokenService;

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

	@GetMapping("/confirm-email")
	public ResponseEntity<?> email(@RequestParam String token) {
		try {
			ConfirmationToken confirmationToken = confirmationTokenService.getValidToken(token);
			confirmationTokenService.confirmToken(confirmationToken);

			URI redirectUri =
					URI.create("https://www.siaeserver.com/siaefrontend/confirmed-email?token=" + confirmationToken.getToken());
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(redirectUri);
			return new ResponseEntity<>(headers, HttpStatus.FOUND);
		} catch (IllegalStateException e) {
			URI redirectUri = URI.create("https://www.siaeserver.com/siaefrontend/invalid-token?reason=expired");
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(redirectUri);
			return new ResponseEntity<>(headers, HttpStatus.FOUND);
		}
	}

	@ExceptionHandler(EmailAlreadyExists.class)
	public ResponseEntity<?> handleEmailAlreadyExists(EmailAlreadyExists ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
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
