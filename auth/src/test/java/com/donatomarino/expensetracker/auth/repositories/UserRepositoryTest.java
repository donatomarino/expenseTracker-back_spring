package com.donatomarino.expensetracker.auth.repositories;

import com.donatomarino.expensetracker.auth.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private final User USER_PREPARED = User.builder()
    .fullName("Donato Marino")
    .email("donato@test.com")
    .password("password")
    .build();

  private void saveUser() {
    userRepository.save(USER_PREPARED);
  }

  @Test
  void shouldFindByEmail() {
    saveUser();
    Optional<User> user = userRepository.findByEmail(USER_PREPARED.getEmail());

    assertTrue(user.isPresent());
    assertEquals(USER_PREPARED.getEmail(), user.get().getEmail());
  }

  @Test
  void shouldReturnEmptyWhenEmailDoesNotExist() {
    Optional<User> user = userRepository.findByEmail(USER_PREPARED.getEmail());

    assertFalse(user.isPresent());
  }

  @Test
  void shouldReturnTrueWhenEmailExists() {
    saveUser();
    boolean userExists = userRepository.existsByEmail(USER_PREPARED.getEmail());

    assertTrue(userExists);
  }

  @Test
  void shouldReturnFalseWhenEmailDoesNotExist() {
    boolean userExists = userRepository.existsByEmail(USER_PREPARED.getEmail());

    assertFalse(userExists);
  }
}
