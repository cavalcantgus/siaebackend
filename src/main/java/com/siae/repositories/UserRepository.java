package com.siae.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.User;
import com.siae.enums.RoleName;

public interface UserRepository extends JpaRepository<User, Long>{
	User findByUsername(String username);
	List<User> findByRoles_Name(RoleName roleName);
	Boolean existsByEmail(String email);
	User findByCpf(String cpf);
}
