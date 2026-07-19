package com.donatomarino.expensetracker.auth.repositories;

import com.donatomarino.expensetracker.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String username);
  boolean existsByEmail(String email);
}
