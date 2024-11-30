package com.siae.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siae.entities.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
