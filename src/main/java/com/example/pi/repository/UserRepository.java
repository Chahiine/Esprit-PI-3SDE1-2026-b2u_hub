package com.example.pi.repository;

import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByActive(boolean active);
    long countByRole(Role role);
}
