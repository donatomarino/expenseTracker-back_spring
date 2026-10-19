package com.donatomarino.expensetracker.auth.services;

import com.donatomarino.expensetracker.auth.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(
      "mysecretkeymysecretkeymysecretkeymysecretkey",
      86400000L
    );
  }

  private final User USER_PREPARED = User.builder()
    .id(1L)
    .fullName("Donato Marino")
    .email("donato@test.com")
    .password("password")
    .build();

  private Map<String, Object> prepareClaims() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", USER_PREPARED.getId());
    return claims;
  }

  private String generateToken() {
    return jwtService.generateToken(prepareClaims(), USER_PREPARED);
  }

  @Test
  void shouldGenerateToken() {
    String token = generateToken();

    assertNotNull(token);
    assertFalse(token.isBlank());
  }

  @Test
  void shouldValidateTokenForSameUser() {
    String token = generateToken();
    boolean isValid = jwtService.isTokenValid(token, USER_PREPARED);

    assertTrue(isValid);
  }

  @Test
  void shouldExtractUsernameFromToken() {
    String token = generateToken();
    String username = jwtService.getUsername(token);

    assertEquals(USER_PREPARED.getEmail(), username);
  }

  @Test
  void shouldExtractClaimFromToken() {
    String token = generateToken();

    Long userId = jwtService.extractClaim(token, claimsMap -> ((Number) claimsMap.get("userId")).longValue());

    assertEquals(USER_PREPARED.getId(), userId);
  }

  @Test
  void shouldRejectTokenForDifferentUser() {
    String token = generateToken();
    User anotherUser = User.builder()
      .id(2L)
      .fullName("Another User")
      .email("another@test.com")
      .password("password")
      .build();

    boolean isValid = jwtService.isTokenValid(token, anotherUser);
    assertFalse(isValid);
  }

  @Test
  void shouldRejectExpiredToken() {
    JwtService shortLivedJwtService = new JwtService(
      "mysecretkeymysecretkeymysecretkeymysecretkey",
      1L);

    String token = shortLivedJwtService.generateToken(prepareClaims(), USER_PREPARED);

    boolean isValid = shortLivedJwtService.isTokenValid(token, USER_PREPARED);
    assertFalse(isValid);
  }

  @Test
  void shouldRejectInvalidToken() {
    String invalidToken = "invalid.token.value";

    boolean isValid = jwtService.isTokenValid(invalidToken, USER_PREPARED);
    assertFalse(isValid);
  }
}
