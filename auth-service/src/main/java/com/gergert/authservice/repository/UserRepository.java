package com.gergert.authservice.repository;

import com.gergert.authservice.entity.User;
import com.gergert.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    long countByRole(Role role);
}
