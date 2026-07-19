package com.donatomarino.expensetracker.auth.services;

import com.donatomarino.expensetracker.auth.dtos.AuthRequest;
import com.donatomarino.expensetracker.auth.dtos.AuthResponse;
import com.donatomarino.expensetracker.auth.dtos.RegisterRequest;
import com.donatomarino.expensetracker.auth.entities.User;
import com.donatomarino.expensetracker.auth.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Value("${app.jwt.expiration:86400000}")
  private long jwtExpiration;

  /**
   * Registers a new user
   *
   * @param request
   * @return JWT token
   */
  @Transactional
  public AuthResponse register(@Valid RegisterRequest request) {
    // Check that email is not already in use
    if (userRepository.existsByEmail(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
    }

    // Create user using Lombok builder
    User user = User.builder()
      .fullName(request.fullName())
      .email(request.email())
      .password(passwordEncoder.encode(request.password()))
      .build();

    userRepository.save(user);

    // Generate token
    String jwtToken = generateToken(user);
    return new AuthResponse(jwtToken, jwtExpiration);
  }

  /**
   * Authenticates a user
   *
   * @param request
   * @return JWT token
   */
  public AuthResponse authenticate(@Valid AuthRequest request) {
    try {
      // Delegate verification to AuthenticationManager
      authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
          request.email(),
          request.password()));
    } catch (BadCredentialsException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    User user = findUserByEmail(request.email());

    // Generate token
    String jwtToken = generateToken(user);

    return new AuthResponse(jwtToken, jwtExpiration);
  }

  /**
   * Refresh token
   *
   * @param authHeader return new JWT token
   */
  public AuthResponse refreshToken(String authHeader) {
    // Check that the header is valid
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    String oldToken = authHeader.substring(7);
    String userEmail = jwtService.getUsername(oldToken);

    User user = findUserByEmail(userEmail);

    // Generate new token
    String newToken = generateToken(user);
    return new AuthResponse(newToken, jwtExpiration);
  }

  /**
   * Find user by email
   *
   * @param email
   * @return User
   */
  public User findUserByEmail(String email) {
    return userRepository.findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException("[Auth] -> [findUserByEmail]: User not found"));
  }

  /**
   * Generate JWT token
   *
   * @param user
   * @return JWT token
   */
  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());

    return jwtService.generateToken(claims, user);
  }

}
