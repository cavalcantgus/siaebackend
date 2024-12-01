package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.Role;
import com.siae.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long>{
	Role findByName(RoleName name);
}
