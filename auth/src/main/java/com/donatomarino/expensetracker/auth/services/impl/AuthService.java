package com.donatomarino.expensetracker.auth.services.impl;

import com.donatomarino.expensetracker.auth.dtos.AuthRequest;
import com.donatomarino.expensetracker.auth.dtos.AuthResponse;
import com.donatomarino.expensetracker.auth.dtos.RegisterRequest;
import com.donatomarino.expensetracker.auth.dtos.RegisterResponse;
import com.donatomarino.expensetracker.auth.entities.User;
import com.donatomarino.expensetracker.auth.repositories.UserRepository;
import com.donatomarino.expensetracker.auth.services.interfaces.IAuthService;
import com.donatomarino.expensetracker.auth.services.interfaces.IJwtService;
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
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final IJwtService jwtService;
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
  public RegisterResponse register(@Valid RegisterRequest request) {
    // Normalize full name
    String normalizedFullName = request.fullName()
      .trim()
      .replaceAll("\\s+", " ");

    // Normalize email
    String normalizedEmail = request.email()
      .trim()
      .toLowerCase(Locale.ROOT);

    // Check that email is not already in use
    if (userRepository.existsByEmail(normalizedEmail)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
    }

    // Create user using Lombok builder
    User user = User.builder()
      .fullName(normalizedFullName)
      .email(normalizedEmail)
      .password(passwordEncoder.encode(request.password()))
      .build();

    userRepository.save(user);

    return new RegisterResponse("User registered successfully", true);
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
    String name = user.getFullName().split(" ")[0];

    // Generate token
    String jwtToken = generateToken(user);

    return new AuthResponse(jwtToken, name, jwtExpiration);
  }

  // /**
  //  * Refresh token
  //  *
  //  * @param authHeader return new JWT token
  //  */
  // public AuthResponse refreshToken(String authHeader) {
  //   // Check that the header is valid
  //   if (authHeader == null || !authHeader.startsWith("Bearer ")) {
  //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
  //   }

  //   String oldToken = authHeader.substring(7);
  //   String userEmail = jwtService.getUsername(oldToken);

  //   User user = findUserByEmail(userEmail);
  //   String name = user.getFullName().split(" ")[0];

  //   // Generate new token
  //   String newToken = generateToken(user);
  //   return new AuthResponse(newToken, name, jwtExpiration);
  // }

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
