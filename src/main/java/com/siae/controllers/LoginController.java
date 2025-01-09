package com.siae.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siae.dto.LoginRequest;
import com.siae.utils.ErrorResponse;
import com.siae.utils.JwtUtil;

@RestController
@RequestMapping("/login")
public class LoginController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/auth")
	 public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
	    try {
	    	 Authentication authentication = authenticationManager.authenticate(
	    	         new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
	    	     );
	    	     
	    	     String role = authentication.getAuthorities().stream()
	    	    		 .findFirst()
	    	    		 .map(GrantedAuthority::getAuthority)
	    	    		 .orElse("USER");

	    	     String token = jwtUtil.generateToken(authentication.getName(), role);
	    	     return ResponseEntity.ok(Map.of("token", "Bearer " + token));
		} catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Usuário ou senha inválidos"));
		} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro interno no servidor"));
        }
	 }

	
}
