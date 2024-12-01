package com.siae.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siae.entities.User;
import com.siae.enums.RoleName;
import com.siae.services.UserService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {
	
	@Autowired UserService userService;
	
	@GetMapping("/pending-users")
	public ResponseEntity<List<User>> listPendingUsers(){
		String rolename = "PENDENTE";
		return ResponseEntity.ok(userService.findUsersByRole(RoleName.valueOf(rolename)));
	}
}
