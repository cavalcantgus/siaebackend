package com.siae.utils;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.siae.dto.LoginRequest;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {
	
	 private final Key key;
	 private static final int TIME_EXPIRATION = 86400000;

	 public JwtUtil(@Value("${api.security.token.secret}") String secretKey) {
		 this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	 }
	
	public String generateToken(String username, String role) {
		return Jwts.builder()
				.setSubject(username)
				.claim("role", role)
				.setIssuer("auth-api")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + TIME_EXPIRATION))
				.signWith(this.key, SignatureAlgorithm.HS256)
				.compact();
	}
	
	public String extractUsername(String token) {
		if(token == null || !token.contains(".")) {
			throw new IllegalArgumentException("Token inválido ou malformatado");
		}
		System.out.println("Token: " + token);
		return Jwts.parserBuilder()
				.setSigningKey(this.key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();	
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
